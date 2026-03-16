# Benchmark Results

**Model:** Qwen3-5-35B-A3B
**Date:** 2026-03-16T01:13:26.799610357
**Score:** 33/33

| Teaser | Status | Tool Calls | Errors | Duration | Details |
|--------|--------|-----------|--------|----------|---------|
| factorial | PASS | 2 | 1 | 21087ms | [detail](factorial.md) |
| fizzbuzz | PASS | 1 | 0 | 6616ms | [detail](fizzbuzz.md) |
| closure_counter | PASS | 1 | 0 | 5315ms | [detail](closure_counter.md) |
| pipe_chain | PASS | 1 | 0 | 4791ms | [detail](pipe_chain.md) |
| recursive_flatten | PASS | 3 | 1 | 14552ms | [detail](recursive_flatten.md) |
| object_transform | PASS | 1 | 0 | 5552ms | [detail](object_transform.md) |
| string_manipulation | PASS | 1 | 0 | 4162ms | [detail](string_manipulation.md) |
| reduce_groupby | PASS | 1 | 0 | 7175ms | [detail](reduce_groupby.md) |
| bitwise_flags | PASS | 1 | 0 | 5749ms | [detail](bitwise_flags.md) |
| scatter_parallel | PASS | 1 | 0 | 4131ms | [detail](scatter_parallel.md) |
| fibonacci_memo | PASS | 2 | 1 | 9011ms | [detail](fibonacci_memo.md) |
| regex_extract | PASS | 1 | 0 | 4382ms | [detail](regex_extract.md) |
| matrix_multiply | PASS | 1 | 0 | 8812ms | [detail](matrix_multiply.md) |
| deep_clone | PASS | 1 | 0 | 10002ms | [detail](deep_clone.md) |
| binary_search | PASS | 2 | 0 | 13314ms | [detail](binary_search.md) |
| curry | PASS | 1 | 0 | 5235ms | [detail](curry.md) |
| linked_list | PASS | 1 | 0 | 7077ms | [detail](linked_list.md) |
| pipe_wordfreq | PASS | 1 | 0 | 4814ms | [detail](pipe_wordfreq.md) |
| roman_numerals | PASS | 1 | 0 | 9226ms | [detail](roman_numerals.md) |
| merge_sort | PASS | 1 | 0 | 12866ms | [detail](merge_sort.md) |
| event_emitter | PASS | 2 | 1 | 13364ms | [detail](event_emitter.md) |
| pipe_csv_parse | PASS | 2 | 1 | 14164ms | [detail](pipe_csv_parse.md) |
| count_letter_r_strawberry | PASS | 1 | 0 | 3497ms | [detail](count_letter_r_strawberry.md) |
| count_letter_l_lullaby | PASS | 1 | 0 | 3321ms | [detail](count_letter_l_lullaby.md) |
| count_words_with_letter | PASS | 1 | 0 | 3823ms | [detail](count_words_with_letter.md) |
| anagram_check | PASS | 1 | 0 | 5217ms | [detail](anagram_check.md) |
| nth_prime | PASS | 1 | 0 | 7649ms | [detail](nth_prime.md) |
| collatz_steps | PASS | 1 | 0 | 5798ms | [detail](collatz_steps.md) |
| digit_sum_power | PASS | 1 | 0 | 4686ms | [detail](digit_sum_power.md) |
| longest_common_subsequence | PASS | 2 | 1 | 19150ms | [detail](longest_common_subsequence.md) |
| balanced_parens | PASS | 1 | 0 | 6588ms | [detail](balanced_parens.md) |
| tower_of_hanoi | PASS | 1 | 0 | 4322ms | [detail](tower_of_hanoi.md) |
| escape_heavy_strings | PASS | 3 | 0 | 19284ms | [detail](escape_heavy_strings.md) |

## Summary

All benchmarks passed.

## Aggregate Stats

| Metric | Value |
|--------|-------|
| Pass rate | 100% (33/33) |
| First-try success | 25/33 |
| Total tool calls | 43 |
| Tool errors | 6 |
| Avg tool calls/teaser | 1.3 |
| Total time | 274s |
| Avg time/teaser | 8s |
| Error recovery rate | 6/6 recovered |
