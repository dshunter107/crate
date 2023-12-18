/*
 * Licensed to Crate.io GmbH ("Crate") under one or more contributor
 * license agreements.  See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.  Crate licenses
 * this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.  You may
 * obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * However, if you have executed another commercial license agreement
 * with Crate these terms will supersede the license and you may use the
 * software solely pursuant to the terms of the relevant commercial agreement.
 */

package io.crate.lucene;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.lucene.search.FieldExistsQuery;
import org.apache.lucene.search.MatchNoDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.junit.Test;

public class BooleanEqQueryTest extends LuceneQueryBuilderTest {
    @Override
    protected String createStmt() {
        // `columnstore = false` is not supported
        return """
                create table m (
                a1 boolean,
                a2 boolean index off
            )
            """;
    }

    @Test
    public void test_BooleanEqQuery_termQuery() {
        Query query = convert("a1 = true");
        assertThat(query).isExactlyInstanceOf(TermQuery.class);
        assertThat(query).hasToString("a1:T");

        query = convert("a2 = true");
        // SortedNumericDocValuesRangeQuery.class is not public
        assertThat(query.getClass().getName()).endsWith("SortedNumericDocValuesRangeQuery");
        assertThat(query).hasToString("a2:[1 TO 1]");
    }

    @Test
    public void test_BooleanEqQuery_rangeQuery() {
        Query query = convert("a1 >= true");
        assertThat(query).isExactlyInstanceOf(TermQuery.class);
        assertThat(query).hasToString("a1:T");

        query = convert("a1 <= true");
        assertThat(query).isExactlyInstanceOf(FieldExistsQuery.class);
        assertThat(query).hasToString("FieldExistsQuery [field=a1]");

        query = convert("a1 > true");
        assertThat(query).isExactlyInstanceOf(MatchNoDocsQuery.class);

        query = convert("a1 < true");
        assertThat(query).isExactlyInstanceOf(TermQuery.class);
        assertThat(query).hasToString("a1:F");

        query = convert("a2 >= true");
        // SortedNumericDocValuesRangeQuery.class is not public
        assertThat(query.getClass().getName()).endsWith("SortedNumericDocValuesRangeQuery");
        assertThat(query).hasToString("a2:[1 TO 1]");

        query = convert("a2 <= true");
        assertThat(query).isExactlyInstanceOf(FieldExistsQuery.class);
        assertThat(query).hasToString("FieldExistsQuery [field=a2]");

        query = convert("a2 > true");
        assertThat(query).isExactlyInstanceOf(MatchNoDocsQuery.class);

        query = convert("a2 < true");
        // SortedNumericDocValuesRangeQuery.class is not public
        assertThat(query.getClass().getName()).endsWith("SortedNumericDocValuesRangeQuery");
        assertThat(query).hasToString("a2:[0 TO 0]");
    }
}
