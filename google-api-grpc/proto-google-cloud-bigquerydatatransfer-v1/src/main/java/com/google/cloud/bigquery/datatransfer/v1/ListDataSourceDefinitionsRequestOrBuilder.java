// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: google/cloud/bigquery/datatransfer/v1/datasource.proto

package com.google.cloud.bigquery.datatransfer.v1;

public interface ListDataSourceDefinitionsRequestOrBuilder
    extends
    // @@protoc_insertion_point(interface_extends:google.cloud.bigquery.datatransfer.v1.ListDataSourceDefinitionsRequest)
    com.google.protobuf.MessageOrBuilder {

  /**
   *
   *
   * <pre>
   * The BigQuery project id for which data sources should be returned.
   * Must be in the form: `projects/{project_id}/locations/{location_id}`
   * </pre>
   *
   * <code>string parent = 1;</code>
   */
  java.lang.String getParent();
  /**
   *
   *
   * <pre>
   * The BigQuery project id for which data sources should be returned.
   * Must be in the form: `projects/{project_id}/locations/{location_id}`
   * </pre>
   *
   * <code>string parent = 1;</code>
   */
  com.google.protobuf.ByteString getParentBytes();

  /**
   *
   *
   * <pre>
   * Pagination token, which can be used to request a specific page
   * of `ListDataSourceDefinitionsRequest` list results. For multiple-page
   * results, `ListDataSourceDefinitionsResponse` outputs a `next_page` token,
   * which can be used as the `page_token` value to request the next page of
   * the list results.
   * </pre>
   *
   * <code>string page_token = 2;</code>
   */
  java.lang.String getPageToken();
  /**
   *
   *
   * <pre>
   * Pagination token, which can be used to request a specific page
   * of `ListDataSourceDefinitionsRequest` list results. For multiple-page
   * results, `ListDataSourceDefinitionsResponse` outputs a `next_page` token,
   * which can be used as the `page_token` value to request the next page of
   * the list results.
   * </pre>
   *
   * <code>string page_token = 2;</code>
   */
  com.google.protobuf.ByteString getPageTokenBytes();

  /**
   *
   *
   * <pre>
   * Page size. The default page size is the maximum value of 1000 results.
   * </pre>
   *
   * <code>int32 page_size = 3;</code>
   */
  int getPageSize();
}
