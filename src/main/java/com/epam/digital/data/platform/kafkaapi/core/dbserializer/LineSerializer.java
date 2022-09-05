/*
 * Copyright 2022 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.digital.data.platform.kafkaapi.core.dbserializer;

import com.epam.digital.data.platform.model.core.geometry.Line;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.util.stream.Collectors;

public class LineSerializer extends StdSerializer<Line> {

  public LineSerializer() {
    super(Line.class);
  }

  @Override
  public void serialize(
      Line line, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
      throws IOException {
    jsonGenerator.writeString(
        String.format("SRID=%d;LINESTRING(%s)", line.getSrid(), getLineDotsFormatted(line)));
  }

  private String getLineDotsFormatted(Line line) {
    return line.getDots().stream()
        .map(dot -> dot.getLongitude() + " " + dot.getLatitude())
        .collect(Collectors.joining(", "));
  }

  @Override
  public void serializeWithType(
      Line line,
      JsonGenerator jsonGenerator,
      SerializerProvider serializers,
      TypeSerializer typeSer)
      throws IOException {
    serialize(line, jsonGenerator, serializers);
  }
}