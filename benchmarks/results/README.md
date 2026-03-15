# Benchmark Results

**Model:** Qwen3-5-35B-A3B
**Date:** 2026-03-15T17:11:21.271385701
**Score:** 32/32

| Teaser | Status | Tool Calls | Errors | Duration | Details |
|--------|--------|-----------|--------|----------|---------|
| factorial | PASS | 1 | 0 | 19193ms | [detail](factorial.md) |
| fizzbuzz | PASS | 1 | 0 | 7195ms | [detail](fizzbuzz.md) |
| closure_counter | PASS | 1 | 0 | 5458ms | [detail](closure_counter.md) |
| pipe_chain | PASS | 1 | 0 | 4879ms | [detail](pipe_chain.md) |
| recursive_flatten | PASS | 1 | 0 | 6680ms | [detail](recursive_flatten.md) |
| object_transform | PASS | 1 | 0 | 5280ms | [detail](object_transform.md) |
| string_manipulation | PASS | 1 | 0 | 4335ms | [detail](string_manipulation.md) |
| reduce_groupby | PASS | 1 | 0 | 6811ms | [detail](reduce_groupby.md) |
| bitwise_flags | PASS | 1 | 0 | 5701ms | [detail](bitwise_flags.md) |
| scatter_parallel | PASS | 1 | 0 | 4185ms | [detail](scatter_parallel.md) |
| fibonacci_memo | PASS | 4 | 0 | 18507ms | [detail](fibonacci_memo.md) |
| regex_extract | PASS | 3 | 0 | 16677ms | [detail](regex_extract.md) |
| matrix_multiply | PASS | 1 | 0 | 9305ms | [detail](matrix_multiply.md) |
| deep_clone | PASS | 1 | 0 | 9784ms | [detail](deep_clone.md) |
| binary_search | PASS | 2 | 0 | 13248ms | [detail](binary_search.md) |
| curry | PASS | 1 | 0 | 4178ms | [detail](curry.md) |
| linked_list | PASS | 1 | 0 | 6592ms | [detail](linked_list.md) |
| pipe_wordfreq | PASS | 1 | 0 | 5052ms | [detail](pipe_wordfreq.md) |
| roman_numerals | PASS | 1 | 0 | 9138ms | [detail](roman_numerals.md) |
| merge_sort | PASS | 2 | 0 | 22380ms | [detail](merge_sort.md) |
| event_emitter | PASS | 1 | 0 | 8721ms | [detail](event_emitter.md) |
| pipe_csv_parse | PASS | 1 | 0 | 9691ms | [detail](pipe_csv_parse.md) |
| count_letter_r_strawberry | PASS | 1 | 0 | 3444ms | [detail](count_letter_r_strawberry.md) |
| count_letter_l_lullaby | PASS | 1 | 0 | 3233ms | [detail](count_letter_l_lullaby.md) |
| count_words_with_letter | PASS | 1 | 0 | 4137ms | [detail](count_words_with_letter.md) |
| anagram_check | PASS | 1 | 0 | 4783ms | [detail](anagram_check.md) |
| nth_prime | PASS | 1 | 0 | 7804ms | [detail](nth_prime.md) |
| collatz_steps | PASS | 1 | 0 | 5628ms | [detail](collatz_steps.md) |
| digit_sum_power | PASS | 1 | 0 | 4545ms | [detail](digit_sum_power.md) |
| longest_common_subsequence | PASS | 1 | 0 | 10915ms | [detail](longest_common_subsequence.md) |
| balanced_parens | PASS | 1 | 0 | 7114ms | [detail](balanced_parens.md) |
| tower_of_hanoi | PASS | 1 | 0 | 3526ms | [detail](tower_of_hanoi.md) |

## Summary

All benchmarks passed.

## Aggregate Stats

| Metric | Value |
|--------|-------|
| Pass rate | 100% (32/32) |
| First-try success | 28/32 |
| Total tool calls | 39 |
| Tool errors | 0 |
| Avg tool calls/teaser | 1.2 |
| Total time | 258s |
| Avg time/teaser | 8s |
| Error recovery rate | N/A (no errors) |
