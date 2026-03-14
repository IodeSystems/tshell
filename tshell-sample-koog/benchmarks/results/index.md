# Benchmark Results

**Model:** Qwen3-5-35B-A3B
**Date:** 2026-03-14T10:28:51.598378697
**Score:** 31/32

| Teaser | Status | Tool Calls | Errors | Duration | Details |
|--------|--------|-----------|--------|----------|---------|
| factorial | PASS | 1 | 0 | 4871ms | [detail](factorial.md) |
| fizzbuzz | PASS | 1 | 0 | 8061ms | [detail](fizzbuzz.md) |
| closure_counter | PASS | 1 | 0 | 9562ms | [detail](closure_counter.md) |
| pipe_chain | PASS | 1 | 0 | 7995ms | [detail](pipe_chain.md) |
| recursive_flatten | PASS | 1 | 0 | 9769ms | [detail](recursive_flatten.md) |
| object_transform | PASS | 1 | 0 | 9233ms | [detail](object_transform.md) |
| string_manipulation | PASS | 1 | 0 | 4031ms | [detail](string_manipulation.md) |
| reduce_groupby | PASS | 1 | 0 | 14159ms | [detail](reduce_groupby.md) |
| bitwise_flags | PASS | 1 | 0 | 11575ms | [detail](bitwise_flags.md) |
| scatter_parallel | PASS | 1 | 0 | 5973ms | [detail](scatter_parallel.md) |
| fibonacci_memo | PASS | 3 | 0 | 16121ms | [detail](fibonacci_memo.md) |
| regex_extract | PASS | 3 | 0 | 17756ms | [detail](regex_extract.md) |
| matrix_multiply | PASS | 1 | 0 | 11297ms | [detail](matrix_multiply.md) |
| deep_clone | PASS | 3 | 0 | 25832ms | [detail](deep_clone.md) |
| binary_search | PASS | 1 | 0 | 12759ms | [detail](binary_search.md) |
| curry | PASS | 1 | 0 | 9085ms | [detail](curry.md) |
| linked_list | FAIL | 4 | 0 | 30004ms | [detail](linked_list.md) (TIMEOUT (30s)) |
| pipe_wordfreq | PASS | 1 | 0 | 5228ms | [detail](pipe_wordfreq.md) |
| roman_numerals | PASS | 1 | 0 | 15072ms | [detail](roman_numerals.md) |
| merge_sort | PASS | 1 | 0 | 19025ms | [detail](merge_sort.md) |
| event_emitter | PASS | 1 | 0 | 14079ms | [detail](event_emitter.md) |
| pipe_csv_parse | PASS | 2 | 0 | 16872ms | [detail](pipe_csv_parse.md) |
| count_letter_r_strawberry | PASS | 1 | 0 | 5309ms | [detail](count_letter_r_strawberry.md) |
| count_letter_l_lullaby | PASS | 1 | 0 | 4010ms | [detail](count_letter_l_lullaby.md) |
| reverse_words_order | PASS | 1 | 0 | 5481ms | [detail](reverse_words_order.md) |
| anagram_check | PASS | 1 | 0 | 10147ms | [detail](anagram_check.md) |
| nth_prime | PASS | 1 | 0 | 8000ms | [detail](nth_prime.md) |
| collatz_steps | PASS | 1 | 0 | 7059ms | [detail](collatz_steps.md) |
| digit_sum_power | PASS | 1 | 0 | 7454ms | [detail](digit_sum_power.md) |
| longest_common_subsequence | PASS | 1 | 0 | 13832ms | [detail](longest_common_subsequence.md) |
| balanced_parens | PASS | 1 | 0 | 8976ms | [detail](balanced_parens.md) |
| tower_of_hanoi | PASS | 1 | 0 | 5671ms | [detail](tower_of_hanoi.md) |

## Summary

Failed: linked_list

## Aggregate Stats

| Metric | Value |
|--------|-------|
| Pass rate | 96% (31/32) |
| First-try success | 27/32 |
| Total tool calls | 42 |
| Tool errors | 0 |
| Avg tool calls/teaser | 1.3 |
| Total time | 354s |
| Avg time/teaser | 11s |
| Error recovery rate | N/A (no errors) |
