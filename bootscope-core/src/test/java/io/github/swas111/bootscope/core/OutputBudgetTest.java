package io.github.swas111.bootscope.core;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OutputBudgetTest {

    @Test
    void keepsSummaryAndAllItemsWhenTheyFit() {
        ToolResponse r = new OutputBudget(4000).fit("2 slow endpoints", List.of("GET /orders", "GET /pay"), "page: 2");
        assertThat(r.summary()).isEqualTo("2 slow endpoints");
        assertThat(r.items()).containsExactly("GET /orders", "GET /pay");
        assertThat(r.truncated()).isFalse();
        assertThat(r.moreHint()).isNull();
    }

    @Test
    void dropsItemsBeyondBudgetAndFlagsTruncatedWithHint() {
        List<String> items = new ArrayList<>();
        for (int i = 0; i < 500; i++) {
            items.add("endpoint-" + i + "-with-some-padding-text");
        }
        OutputBudget budget = new OutputBudget(200);
        ToolResponse r = budget.fit("many endpoints", items, "call again with page: 2");

        assertThat(r.truncated()).isTrue();
        assertThat(r.moreHint()).isEqualTo("call again with page: 2");
        assertThat(r.items().size()).isLessThan(items.size());
        assertThat(r.render().length()).isLessThanOrEqualTo(200);
    }

    @Test
    void renderedOutputNeverExceedsBudgetEvenWhenTruncated() {
        List<String> items = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            items.add("x".repeat(50));
        }
        for (int max : new int[] {64, 100, 200, 1000, 4000}) {
            ToolResponse r = new OutputBudget(max).fit("summary", items, "page: 2");
            assertThat(r.render().length())
                    .as("rendered length within budget %d", max)
                    .isLessThanOrEqualTo(max);
        }
    }

    @Test
    void hardTruncatesAnOversizedSummary() {
        String huge = "s".repeat(500);
        ToolResponse r = new OutputBudget(100).fit(huge, List.of("ignored"), "page: 2");
        assertThat(r.truncated()).isTrue();
        assertThat(r.render().length()).isLessThanOrEqualTo(100);
        assertThat(r.summary()).endsWith("…");
    }

    @Test
    void rejectsTooSmallBudget() {
        assertThatThrownBy(() -> new OutputBudget(5)).isInstanceOf(IllegalArgumentException.class);
    }
}
