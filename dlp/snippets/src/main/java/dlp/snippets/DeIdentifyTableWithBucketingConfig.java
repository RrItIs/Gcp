/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dlp.snippets;

// [START dlp_deidentify_table_primitive_bucketing]

import com.google.cloud.dlp.v2.DlpServiceClient;
import com.google.privacy.dlp.v2.BucketingConfig;
import com.google.privacy.dlp.v2.ContentItem;
import com.google.privacy.dlp.v2.DeidentifyConfig;
import com.google.privacy.dlp.v2.DeidentifyContentRequest;
import com.google.privacy.dlp.v2.DeidentifyContentResponse;
import com.google.privacy.dlp.v2.FieldId;
import com.google.privacy.dlp.v2.FieldTransformation;
import com.google.privacy.dlp.v2.LocationName;
import com.google.privacy.dlp.v2.PrimitiveTransformation;
import com.google.privacy.dlp.v2.RecordTransformations;
import com.google.privacy.dlp.v2.Table;
import com.google.privacy.dlp.v2.Value;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DeIdentifyTableWithBucketingConfig {
  public static void main(String[] args) throws Exception {

    // TODO(developer): Replace these variables before running the sample.
    // The Google Cloud project id to use as a parent resource.
    String projectId = "your-project-id";
    // Specify the table to be considered for de-identification.
    Table tableToDeIdentify =
        Table.newBuilder()
            .addHeaders(FieldId.newBuilder().setName("AGE").build())
            .addHeaders(FieldId.newBuilder().setName("PATIENT").build())
            .addHeaders(FieldId.newBuilder().setName("HAPPINESS SCORE").build())
            .addRows(
                Table.Row.newBuilder()
                    .addValues(Value.newBuilder().setStringValue("101").build())
                    .addValues(Value.newBuilder().setStringValue("Charles Dickens").build())
                    .addValues(Value.newBuilder().setIntegerValue(95).build())
                    .build())
            .addRows(
                Table.Row.newBuilder()
                    .addValues(Value.newBuilder().setStringValue("22").build())
                    .addValues(Value.newBuilder().setStringValue("Jane Austen").build())
                    .addValues(Value.newBuilder().setIntegerValue(21).build())
                    .build())
            .addRows(
                Table.Row.newBuilder()
                    .addValues(Value.newBuilder().setStringValue("55").build())
                    .addValues(Value.newBuilder().setStringValue("Mark Twain").build())
                    .addValues(Value.newBuilder().setIntegerValue(75).build())
                    .build())
            .build();

    deIdentifyTableBucketing(projectId, tableToDeIdentify);
  }

  // Performs data de-identification on a table by replacing the values within each bucket with
  // predefined replacement values.
  public static Table deIdentifyTableBucketing(String projectId, Table tableToDeIdentify)
      throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (DlpServiceClient dlp = DlpServiceClient.create()) {
      // Specify what content you want the service to de-identify.
      ContentItem contentItem = ContentItem.newBuilder().setTable(tableToDeIdentify).build();

      List<BucketingConfig.Bucket> buckets = new ArrayList<>();
      buckets.add(
          BucketingConfig.Bucket.newBuilder()
              .setMin(Value.newBuilder().setIntegerValue(0).build())
              .setMax(Value.newBuilder().setIntegerValue(25).build())
              .setReplacementValue(Value.newBuilder().setStringValue("low").build())
              .build());
      buckets.add(
          BucketingConfig.Bucket.newBuilder()
              .setMin(Value.newBuilder().setIntegerValue(25).build())
              .setMax(Value.newBuilder().setIntegerValue(75).build())
              .setReplacementValue(Value.newBuilder().setStringValue("Medium").build())
              .build());
      buckets.add(
          BucketingConfig.Bucket.newBuilder()
              .setMin(Value.newBuilder().setIntegerValue(75).build())
              .setMax(Value.newBuilder().setIntegerValue(100).build())
              .setReplacementValue(Value.newBuilder().setStringValue("High").build())
              .build());

      BucketingConfig bucketingConfig = BucketingConfig.newBuilder().addAllBuckets(buckets).build();

      PrimitiveTransformation primitiveTransformation =
          PrimitiveTransformation.newBuilder().setBucketingConfig(bucketingConfig).build();

      // Specify the field of the table to be de-identified.
      FieldId fieldId = FieldId.newBuilder().setName("HAPPINESS SCORE").build();

      FieldTransformation fieldTransformation =
          FieldTransformation.newBuilder()
              .setPrimitiveTransformation(primitiveTransformation)
              .addFields(fieldId)
              .build();
      RecordTransformations transformations =
          RecordTransformations.newBuilder().addFieldTransformations(fieldTransformation).build();

      DeidentifyConfig deidentifyConfig =
          DeidentifyConfig.newBuilder().setRecordTransformations(transformations).build();

      // Combine configurations into a request for the service.
      DeidentifyContentRequest request =
          DeidentifyContentRequest.newBuilder()
              .setParent(LocationName.of(projectId, "global").toString())
              .setItem(contentItem)
              .setDeidentifyConfig(deidentifyConfig)
              .build();

      // Send the request and receive response from the service.
      DeidentifyContentResponse response = dlp.deidentifyContent(request);

      // Print the results.
      System.out.println("Table after de-identification: " + response.getItem().getTable());
      return response.getItem().getTable();
    }
  }
}
// [END dlp_deidentify_table_primitive_bucketing]
