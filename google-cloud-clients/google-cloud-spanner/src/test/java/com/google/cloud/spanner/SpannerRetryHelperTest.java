/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.cloud.spanner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import io.grpc.Context;
import io.grpc.Context.CancellableContext;
import io.grpc.Deadline;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class SpannerRetryHelperTest {

  @Test
  public void testCancelledContext() {
    final CancellableContext withCancellation = Context.current().withCancellation();
    final Callable<Integer> callable =
        new Callable<Integer>() {
          @Override
          public Integer call() throws Exception {
            throw SpannerExceptionFactory.newSpannerException(ErrorCode.ABORTED, "test");
          }
        };
    ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
    service.schedule(
        new Callable<Void>() {
          @Override
          public Void call() throws Exception {
            withCancellation.cancel(new InterruptedException());
            return null;
          }
        },
        30L,
        TimeUnit.MILLISECONDS);
    try {
      withCancellation.run(
          new Runnable() {
            @Override
            public void run() {
              assertThat(SpannerRetryHelper.runTxWithRetriesOnAborted(callable), is(equalTo(2)));
            }
          });
      fail("missing expected exception");
    } catch (SpannerException e) {
      if (e.getErrorCode() != ErrorCode.CANCELLED) {
        fail(
            String.format(
                "unexpected error %s, expected %s",
                e.getErrorCode().name(), ErrorCode.CANCELLED.name()));
      }
    }
  }

  @Test
  public void testTimedoutContext() {
    ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
    final CancellableContext withDeadline =
        Context.current().withDeadline(Deadline.after(30L, TimeUnit.MILLISECONDS), service);
    final Callable<Integer> callable =
        new Callable<Integer>() {
          @Override
          public Integer call() throws Exception {
            throw SpannerExceptionFactory.newSpannerException(ErrorCode.ABORTED, "test");
          }
        };
    try {
      withDeadline.run(
          new Runnable() {
            @Override
            public void run() {
              assertThat(SpannerRetryHelper.runTxWithRetriesOnAborted(callable), is(equalTo(2)));
            }
          });
      fail("missing expected exception");
    } catch (SpannerException e) {
      if (e.getErrorCode() != ErrorCode.DEADLINE_EXCEEDED) {
        fail(
            String.format(
                "unexpected error %s, expected %s",
                e.getErrorCode().name(), ErrorCode.DEADLINE_EXCEEDED.name()));
      }
    }
  }

  @Test
  public void noException() {
    Callable<Integer> callable =
        new Callable<Integer>() {
          @Override
          public Integer call() throws Exception {
            return 1 + 1;
          }
        };
    assertThat(SpannerRetryHelper.runTxWithRetriesOnAborted(callable), is(equalTo(2)));
  }

  @Test(expected = IllegalStateException.class)
  public void propagateUncheckedException() {
    Callable<Integer> callable =
        new Callable<Integer>() {
          @Override
          public Integer call() throws Exception {
            throw new IllegalStateException("test");
          }
        };
    SpannerRetryHelper.runTxWithRetriesOnAborted(callable);
  }

  @Test
  public void retryOnAborted() {
    final AtomicInteger attempts = new AtomicInteger();
    Callable<Integer> callable =
        new Callable<Integer>() {
          @Override
          public Integer call() throws Exception {
            if (attempts.getAndIncrement() == 0) {
              throw SpannerExceptionFactory.newSpannerException(ErrorCode.ABORTED, "test");
            }
            return 1 + 1;
          }
        };
    assertThat(SpannerRetryHelper.runTxWithRetriesOnAborted(callable), is(equalTo(2)));
  }

  @Test
  public void retryMultipleTimesOnAborted() {
    final AtomicInteger attempts = new AtomicInteger();
    Callable<Integer> callable =
        new Callable<Integer>() {
          @Override
          public Integer call() throws Exception {
            if (attempts.getAndIncrement() < 2) {
              throw SpannerExceptionFactory.newSpannerException(ErrorCode.ABORTED, "test");
            }
            return 1 + 1;
          }
        };
    assertThat(SpannerRetryHelper.runTxWithRetriesOnAborted(callable), is(equalTo(2)));
  }

  @Test(expected = IllegalStateException.class)
  public void retryOnAbortedAndThenPropagateUnchecked() {
    final AtomicInteger attempts = new AtomicInteger();
    Callable<Integer> callable =
        new Callable<Integer>() {
          @Override
          public Integer call() throws Exception {
            if (attempts.getAndIncrement() == 0) {
              throw SpannerExceptionFactory.newSpannerException(ErrorCode.ABORTED, "test");
            }
            throw new IllegalStateException("test");
          }
        };
    SpannerRetryHelper.runTxWithRetriesOnAborted(callable);
  }
}
