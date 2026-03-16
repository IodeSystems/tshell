# Benchmark Results

**Model:** Qwen3-5-35B-A3B
**Date:** 2026-03-16T00:52:35.10899017
**Score:** 32/33

| Teaser | Status | Tool Calls | Errors | Duration | Details |
|--------|--------|-----------|--------|----------|---------|
| factorial | PASS | 1 | 0 | 19077ms | [detail](factorial.md) |
| fizzbuzz | PASS | 1 | 0 | 6448ms | [detail](fizzbuzz.md) |
| closure_counter | PASS | 1 | 0 | 5462ms | [detail](closure_counter.md) |
| pipe_chain | PASS | 1 | 0 | 4806ms | [detail](pipe_chain.md) |
| recursive_flatten | PASS | 1 | 0 | 6861ms | [detail](recursive_flatten.md) |
| object_transform | PASS | 1 | 0 | 5431ms | [detail](object_transform.md) |
| string_manipulation | PASS | 1 | 0 | 4032ms | [detail](string_manipulation.md) |
| reduce_groupby | PASS | 1 | 0 | 7106ms | [detail](reduce_groupby.md) |
| bitwise_flags | PASS | 1 | 0 | 5689ms | [detail](bitwise_flags.md) |
| scatter_parallel | PASS | 1 | 0 | 4097ms | [detail](scatter_parallel.md) |
| fibonacci_memo | PASS | 3 | 2 | 13109ms | [detail](fibonacci_memo.md) |
| regex_extract | PASS | 3 | 0 | 12354ms | [detail](regex_extract.md) |
| matrix_multiply | PASS | 1 | 0 | 9375ms | [detail](matrix_multiply.md) |
| deep_clone | PASS | 2 | 0 | 18110ms | [detail](deep_clone.md) |
| binary_search | PASS | 2 | 0 | 14165ms | [detail](binary_search.md) |
| curry | PASS | 1 | 0 | 4305ms | [detail](curry.md) |
| linked_list | PASS | 2 | 1 | 10072ms | [detail](linked_list.md) |
| pipe_wordfreq | PASS | 1 | 0 | 4828ms | [detail](pipe_wordfreq.md) |
| roman_numerals | PASS | 1 | 0 | 9484ms | [detail](roman_numerals.md) |
| merge_sort | PASS | 2 | 1 | 21759ms | [detail](merge_sort.md) |
| event_emitter | PASS | 1 | 0 | 7541ms | [detail](event_emitter.md) |
| pipe_csv_parse | PASS | 3 | 2 | 20655ms | [detail](pipe_csv_parse.md) |
| count_letter_r_strawberry | PASS | 1 | 0 | 3348ms | [detail](count_letter_r_strawberry.md) |
| count_letter_l_lullaby | PASS | 1 | 0 | 3380ms | [detail](count_letter_l_lullaby.md) |
| count_words_with_letter | PASS | 1 | 0 | 3841ms | [detail](count_words_with_letter.md) |
| anagram_check | PASS | 1 | 0 | 5215ms | [detail](anagram_check.md) |
| nth_prime | PASS | 1 | 0 | 7323ms | [detail](nth_prime.md) |
| collatz_steps | PASS | 1 | 0 | 5584ms | [detail](collatz_steps.md) |
| digit_sum_power | PASS | 3 | 2 | 9345ms | [detail](digit_sum_power.md) |
| longest_common_subsequence | PASS | 1 | 0 | 9057ms | [detail](longest_common_subsequence.md) |
| balanced_parens | PASS | 1 | 0 | 7443ms | [detail](balanced_parens.md) |
| tower_of_hanoi | PASS | 1 | 0 | 3566ms | [detail](tower_of_hanoi.md) |
| escape_heavy_strings | FAIL | 11 | 11 | 60004ms | [detail](escape_heavy_strings.md) (TIMEOUT (60s)) |

## Summary

Failed: escape_heavy_strings

## Aggregate Stats

| Metric | Value |
|--------|-------|
| Pass rate | 96% (32/33) |
| First-try success | 24/33 |
| Total tool calls | 55 |
| Tool errors | 19 |
| Avg tool calls/teaser | 1.7 |
| Total time | 332s |
| Avg time/teaser | 10s |
| Error recovery rate | 5/19 recovered |
