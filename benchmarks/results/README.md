# Benchmark Results

**Model:** Qwen3-5-35B-A3B
**Date:** 2026-03-15T23:11:16.862888708
**Score:** 32/32

| Teaser | Status | Tool Calls | Errors | Duration | Details |
|--------|--------|-----------|--------|----------|---------|
| factorial | PASS | 1 | 0 | 19320ms | [detail](factorial.md) |
| fizzbuzz | PASS | 1 | 0 | 6565ms | [detail](fizzbuzz.md) |
| closure_counter | PASS | 2 | 0 | 9215ms | [detail](closure_counter.md) |
| pipe_chain | PASS | 1 | 0 | 4899ms | [detail](pipe_chain.md) |
| recursive_flatten | PASS | 2 | 0 | 10600ms | [detail](recursive_flatten.md) |
| object_transform | PASS | 1 | 0 | 5343ms | [detail](object_transform.md) |
| string_manipulation | PASS | 1 | 0 | 4431ms | [detail](string_manipulation.md) |
| reduce_groupby | PASS | 1 | 0 | 7116ms | [detail](reduce_groupby.md) |
| bitwise_flags | PASS | 1 | 0 | 5961ms | [detail](bitwise_flags.md) |
| scatter_parallel | PASS | 1 | 0 | 4211ms | [detail](scatter_parallel.md) |
| fibonacci_memo | PASS | 3 | 0 | 14724ms | [detail](fibonacci_memo.md) |
| regex_extract | PASS | 3 | 0 | 14835ms | [detail](regex_extract.md) |
| matrix_multiply | PASS | 1 | 0 | 9063ms | [detail](matrix_multiply.md) |
| deep_clone | PASS | 2 | 0 | 17963ms | [detail](deep_clone.md) |
| binary_search | PASS | 1 | 0 | 7856ms | [detail](binary_search.md) |
| curry | PASS | 2 | 0 | 6613ms | [detail](curry.md) |
| linked_list | PASS | 2 | 0 | 9936ms | [detail](linked_list.md) |
| pipe_wordfreq | PASS | 1 | 0 | 5175ms | [detail](pipe_wordfreq.md) |
| roman_numerals | PASS | 1 | 0 | 9327ms | [detail](roman_numerals.md) |
| merge_sort | PASS | 1 | 0 | 12478ms | [detail](merge_sort.md) |
| event_emitter | PASS | 2 | 0 | 16794ms | [detail](event_emitter.md) |
| pipe_csv_parse | PASS | 3 | 0 | 20571ms | [detail](pipe_csv_parse.md) |
| count_letter_r_strawberry | PASS | 1 | 0 | 3434ms | [detail](count_letter_r_strawberry.md) |
| count_letter_l_lullaby | PASS | 1 | 0 | 3437ms | [detail](count_letter_l_lullaby.md) |
| count_words_with_letter | PASS | 1 | 0 | 3797ms | [detail](count_words_with_letter.md) |
| anagram_check | PASS | 1 | 0 | 4743ms | [detail](anagram_check.md) |
| nth_prime | PASS | 1 | 0 | 7875ms | [detail](nth_prime.md) |
| collatz_steps | PASS | 1 | 0 | 5574ms | [detail](collatz_steps.md) |
| digit_sum_power | PASS | 2 | 0 | 6729ms | [detail](digit_sum_power.md) |
| longest_common_subsequence | PASS | 1 | 0 | 9518ms | [detail](longest_common_subsequence.md) |
| balanced_parens | PASS | 1 | 0 | 6767ms | [detail](balanced_parens.md) |
| tower_of_hanoi | PASS | 1 | 0 | 3514ms | [detail](tower_of_hanoi.md) |

## Summary

All benchmarks passed.

## Aggregate Stats

| Metric | Value |
|--------|-------|
| Pass rate | 100% (32/32) |
| First-try success | 22/32 |
| Total tool calls | 45 |
| Tool errors | 0 |
| Avg tool calls/teaser | 1.4 |
| Total time | 278s |
| Avg time/teaser | 8s |
| Error recovery rate | N/A (no errors) |
