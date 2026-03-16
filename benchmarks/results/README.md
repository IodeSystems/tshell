# Benchmark Results

**Model:** Qwen3-5-35B-A3B
**Date:** 2026-03-16T00:24:06.390392146
**Score:** 32/33

| Teaser | Status | Tool Calls | Errors | Duration | Details |
|--------|--------|-----------|--------|----------|---------|
| factorial | PASS | 2 | 1 | 21835ms | [detail](factorial.md) |
| fizzbuzz | PASS | 1 | 0 | 6614ms | [detail](fizzbuzz.md) |
| closure_counter | PASS | 1 | 0 | 5715ms | [detail](closure_counter.md) |
| pipe_chain | PASS | 1 | 0 | 4960ms | [detail](pipe_chain.md) |
| recursive_flatten | PASS | 1 | 0 | 6652ms | [detail](recursive_flatten.md) |
| object_transform | PASS | 1 | 0 | 5632ms | [detail](object_transform.md) |
| string_manipulation | PASS | 1 | 0 | 4341ms | [detail](string_manipulation.md) |
| reduce_groupby | PASS | 1 | 0 | 7945ms | [detail](reduce_groupby.md) |
| bitwise_flags | PASS | 1 | 0 | 5842ms | [detail](bitwise_flags.md) |
| scatter_parallel | PASS | 1 | 0 | 4226ms | [detail](scatter_parallel.md) |
| fibonacci_memo | PASS | 3 | 2 | 13586ms | [detail](fibonacci_memo.md) |
| regex_extract | PASS | 1 | 0 | 4313ms | [detail](regex_extract.md) |
| matrix_multiply | PASS | 1 | 0 | 9536ms | [detail](matrix_multiply.md) |
| deep_clone | PASS | 4 | 3 | 34553ms | [detail](deep_clone.md) |
| binary_search | PASS | 1 | 0 | 7472ms | [detail](binary_search.md) |
| curry | PASS | 7 | 6 | 20563ms | [detail](curry.md) |
| linked_list | PASS | 1 | 0 | 6368ms | [detail](linked_list.md) |
| pipe_wordfreq | PASS | 3 | 2 | 11788ms | [detail](pipe_wordfreq.md) |
| roman_numerals | PASS | 1 | 0 | 9207ms | [detail](roman_numerals.md) |
| merge_sort | PASS | 1 | 0 | 13182ms | [detail](merge_sort.md) |
| event_emitter | PASS | 2 | 1 | 13234ms | [detail](event_emitter.md) |
| pipe_csv_parse | PASS | 3 | 2 | 22762ms | [detail](pipe_csv_parse.md) |
| count_letter_r_strawberry | PASS | 1 | 0 | 3374ms | [detail](count_letter_r_strawberry.md) |
| count_letter_l_lullaby | PASS | 1 | 0 | 3528ms | [detail](count_letter_l_lullaby.md) |
| count_words_with_letter | FAIL | 1 | 0 | 3724ms | [detail](count_words_with_letter.md) |
| anagram_check | PASS | 1 | 0 | 4743ms | [detail](anagram_check.md) |
| nth_prime | PASS | 1 | 0 | 7328ms | [detail](nth_prime.md) |
| collatz_steps | PASS | 1 | 0 | 5984ms | [detail](collatz_steps.md) |
| digit_sum_power | PASS | 6 | 2 | 21040ms | [detail](digit_sum_power.md) |
| longest_common_subsequence | PASS | 2 | 1 | 19258ms | [detail](longest_common_subsequence.md) |
| balanced_parens | PASS | 1 | 0 | 7157ms | [detail](balanced_parens.md) |
| tower_of_hanoi | PASS | 1 | 0 | 4048ms | [detail](tower_of_hanoi.md) |
| escape_heavy_strings | PASS | 3 | 0 | 20675ms | [detail](escape_heavy_strings.md) |

## Summary

Failed: count_words_with_letter

## Aggregate Stats

| Metric | Value |
|--------|-------|
| Pass rate | 96% (32/33) |
| First-try success | 22/33 |
| Total tool calls | 58 |
| Tool errors | 20 |
| Avg tool calls/teaser | 1.8 |
| Total time | 341s |
| Avg time/teaser | 10s |
| Error recovery rate | 9/20 recovered |
