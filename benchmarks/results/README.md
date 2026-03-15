# Benchmark Results

**Model:** Qwen3-5-35B-A3B
**Date:** 2026-03-15T23:55:17.918837806
**Score:** 32/33

| Teaser | Status | Tool Calls | Errors | Duration | Details |
|--------|--------|-----------|--------|----------|---------|
| factorial | PASS | 1 | 0 | 19577ms | [detail](factorial.md) |
| fizzbuzz | PASS | 1 | 0 | 6856ms | [detail](fizzbuzz.md) |
| closure_counter | PASS | 1 | 0 | 5690ms | [detail](closure_counter.md) |
| pipe_chain | PASS | 1 | 0 | 4798ms | [detail](pipe_chain.md) |
| recursive_flatten | PASS | 1 | 0 | 6396ms | [detail](recursive_flatten.md) |
| object_transform | PASS | 1 | 0 | 5144ms | [detail](object_transform.md) |
| string_manipulation | PASS | 1 | 0 | 4039ms | [detail](string_manipulation.md) |
| reduce_groupby | PASS | 1 | 0 | 6627ms | [detail](reduce_groupby.md) |
| bitwise_flags | PASS | 1 | 0 | 5655ms | [detail](bitwise_flags.md) |
| scatter_parallel | PASS | 1 | 0 | 4058ms | [detail](scatter_parallel.md) |
| fibonacci_memo | PASS | 3 | 0 | 13895ms | [detail](fibonacci_memo.md) |
| regex_extract | PASS | 6 | 0 | 21421ms | [detail](regex_extract.md) |
| matrix_multiply | PASS | 1 | 0 | 7713ms | [detail](matrix_multiply.md) |
| deep_clone | PASS | 2 | 0 | 17125ms | [detail](deep_clone.md) |
| binary_search | FAIL | 3 | 0 | 60004ms | [detail](binary_search.md) (TIMEOUT (60s)) |
| curry | PASS | 1 | 0 | 8841ms | [detail](curry.md) |
| linked_list | PASS | 2 | 0 | 11351ms | [detail](linked_list.md) |
| pipe_wordfreq | PASS | 1 | 0 | 4758ms | [detail](pipe_wordfreq.md) |
| roman_numerals | PASS | 2 | 0 | 21035ms | [detail](roman_numerals.md) |
| merge_sort | PASS | 1 | 0 | 14227ms | [detail](merge_sort.md) |
| event_emitter | PASS | 1 | 0 | 8704ms | [detail](event_emitter.md) |
| pipe_csv_parse | PASS | 3 | 0 | 20711ms | [detail](pipe_csv_parse.md) |
| count_letter_r_strawberry | PASS | 1 | 0 | 3312ms | [detail](count_letter_r_strawberry.md) |
| count_letter_l_lullaby | PASS | 1 | 0 | 3118ms | [detail](count_letter_l_lullaby.md) |
| count_words_with_letter | PASS | 1 | 0 | 3920ms | [detail](count_words_with_letter.md) |
| anagram_check | PASS | 1 | 0 | 4783ms | [detail](anagram_check.md) |
| nth_prime | PASS | 1 | 0 | 8166ms | [detail](nth_prime.md) |
| collatz_steps | PASS | 1 | 0 | 5566ms | [detail](collatz_steps.md) |
| digit_sum_power | PASS | 1 | 0 | 3974ms | [detail](digit_sum_power.md) |
| longest_common_subsequence | PASS | 1 | 0 | 10140ms | [detail](longest_common_subsequence.md) |
| balanced_parens | PASS | 1 | 0 | 6639ms | [detail](balanced_parens.md) |
| tower_of_hanoi | PASS | 1 | 0 | 3711ms | [detail](tower_of_hanoi.md) |
| escape_heavy_strings | PASS | 1 | 0 | 8773ms | [detail](escape_heavy_strings.md) |

## Summary

Failed: binary_search

## Aggregate Stats

| Metric | Value |
|--------|-------|
| Pass rate | 96% (32/33) |
| First-try success | 26/33 |
| Total tool calls | 47 |
| Tool errors | 0 |
| Avg tool calls/teaser | 1.4 |
| Total time | 340s |
| Avg time/teaser | 10s |
| Error recovery rate | N/A (no errors) |
