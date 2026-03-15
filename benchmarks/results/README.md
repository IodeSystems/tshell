# Benchmark Results

**Model:** Qwen3-5-35B-A3B
**Date:** 2026-03-15T23:39:33.855118812
**Score:** 31/33

| Teaser | Status | Tool Calls | Errors | Duration | Details |
|--------|--------|-----------|--------|----------|---------|
| factorial | PASS | 2 | 0 | 7112ms | [detail](factorial.md) |
| fizzbuzz | PASS | 1 | 0 | 6213ms | [detail](fizzbuzz.md) |
| closure_counter | PASS | 1 | 0 | 5067ms | [detail](closure_counter.md) |
| pipe_chain | PASS | 1 | 0 | 4389ms | [detail](pipe_chain.md) |
| recursive_flatten | PASS | 1 | 0 | 5228ms | [detail](recursive_flatten.md) |
| object_transform | PASS | 1 | 0 | 4664ms | [detail](object_transform.md) |
| string_manipulation | PASS | 1 | 0 | 3968ms | [detail](string_manipulation.md) |
| reduce_groupby | PASS | 1 | 0 | 6426ms | [detail](reduce_groupby.md) |
| bitwise_flags | PASS | 1 | 0 | 5214ms | [detail](bitwise_flags.md) |
| scatter_parallel | PASS | 1 | 0 | 3867ms | [detail](scatter_parallel.md) |
| fibonacci_memo | PASS | 3 | 0 | 13377ms | [detail](fibonacci_memo.md) |
| regex_extract | PASS | 2 | 0 | 12179ms | [detail](regex_extract.md) |
| matrix_multiply | PASS | 4 | 0 | 20405ms | [detail](matrix_multiply.md) |
| deep_clone | PASS | 1 | 0 | 9268ms | [detail](deep_clone.md) |
| binary_search | PASS | 1 | 0 | 7638ms | [detail](binary_search.md) |
| curry | PASS | 2 | 0 | 8419ms | [detail](curry.md) |
| linked_list | PASS | 1 | 0 | 6216ms | [detail](linked_list.md) |
| pipe_wordfreq | PASS | 1 | 0 | 4574ms | [detail](pipe_wordfreq.md) |
| roman_numerals | PASS | 1 | 0 | 9099ms | [detail](roman_numerals.md) |
| merge_sort | FAIL | 3 | 0 | 30004ms | [detail](merge_sort.md) (TIMEOUT (30s)) |
| event_emitter | PASS | 1 | 0 | 13484ms | [detail](event_emitter.md) |
| pipe_csv_parse | PASS | 3 | 0 | 18626ms | [detail](pipe_csv_parse.md) |
| count_letter_r_strawberry | PASS | 1 | 0 | 3052ms | [detail](count_letter_r_strawberry.md) |
| count_letter_l_lullaby | PASS | 1 | 0 | 3072ms | [detail](count_letter_l_lullaby.md) |
| count_words_with_letter | PASS | 1 | 0 | 3710ms | [detail](count_words_with_letter.md) |
| anagram_check | PASS | 1 | 0 | 4402ms | [detail](anagram_check.md) |
| nth_prime | PASS | 1 | 0 | 7339ms | [detail](nth_prime.md) |
| collatz_steps | PASS | 1 | 0 | 5215ms | [detail](collatz_steps.md) |
| digit_sum_power | PASS | 1 | 0 | 4249ms | [detail](digit_sum_power.md) |
| longest_common_subsequence | PASS | 1 | 0 | 10337ms | [detail](longest_common_subsequence.md) |
| balanced_parens | PASS | 1 | 0 | 6673ms | [detail](balanced_parens.md) |
| tower_of_hanoi | PASS | 2 | 0 | 5285ms | [detail](tower_of_hanoi.md) |
| escape_heavy_strings | FAIL | 10 | 0 | 60002ms | [detail](escape_heavy_strings.md) (TIMEOUT (60s)) |

## Summary

Failed: merge_sort, escape_heavy_strings

## Aggregate Stats

| Metric | Value |
|--------|-------|
| Pass rate | 93% (31/33) |
| First-try success | 24/33 |
| Total tool calls | 55 |
| Tool errors | 0 |
| Avg tool calls/teaser | 1.7 |
| Total time | 318s |
| Avg time/teaser | 9s |
| Error recovery rate | N/A (no errors) |
