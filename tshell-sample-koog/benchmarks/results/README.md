# Benchmark Results

**Model:** Qwen3-5-35B-A3B
**Date:** 2026-03-14T16:21:17.389407975
**Score:** 32/32

| Teaser | Status | Tool Calls | Errors | Duration | Details |
|--------|--------|-----------|--------|----------|---------|
| factorial | PASS | 1 | 0 | 20120ms | [detail](factorial.md) |
| fizzbuzz | PASS | 1 | 0 | 7184ms | [detail](fizzbuzz.md) |
| closure_counter | PASS | 1 | 0 | 5372ms | [detail](closure_counter.md) |
| pipe_chain | PASS | 1 | 0 | 4943ms | [detail](pipe_chain.md) |
| recursive_flatten | PASS | 1 | 0 | 8369ms | [detail](recursive_flatten.md) |
| object_transform | PASS | 1 | 0 | 5403ms | [detail](object_transform.md) |
| string_manipulation | PASS | 1 | 0 | 4095ms | [detail](string_manipulation.md) |
| reduce_groupby | PASS | 3 | 0 | 15807ms | [detail](reduce_groupby.md) |
| bitwise_flags | PASS | 1 | 0 | 5810ms | [detail](bitwise_flags.md) |
| scatter_parallel | PASS | 1 | 0 | 4197ms | [detail](scatter_parallel.md) |
| fibonacci_memo | PASS | 3 | 0 | 12800ms | [detail](fibonacci_memo.md) |
| regex_extract | PASS | 4 | 0 | 17091ms | [detail](regex_extract.md) |
| matrix_multiply | PASS | 1 | 0 | 8528ms | [detail](matrix_multiply.md) |
| deep_clone | PASS | 1 | 0 | 10007ms | [detail](deep_clone.md) |
| binary_search | PASS | 1 | 0 | 8609ms | [detail](binary_search.md) |
| curry | PASS | 1 | 0 | 4232ms | [detail](curry.md) |
| linked_list | PASS | 1 | 0 | 9396ms | [detail](linked_list.md) |
| pipe_wordfreq | PASS | 1 | 0 | 5328ms | [detail](pipe_wordfreq.md) |
| roman_numerals | PASS | 1 | 0 | 9369ms | [detail](roman_numerals.md) |
| merge_sort | PASS | 1 | 0 | 13266ms | [detail](merge_sort.md) |
| event_emitter | PASS | 1 | 0 | 7270ms | [detail](event_emitter.md) |
| pipe_csv_parse | PASS | 2 | 0 | 14667ms | [detail](pipe_csv_parse.md) |
| count_letter_r_strawberry | PASS | 1 | 0 | 3385ms | [detail](count_letter_r_strawberry.md) |
| count_letter_l_lullaby | PASS | 1 | 0 | 3537ms | [detail](count_letter_l_lullaby.md) |
| reverse_words_order | PASS | 1 | 0 | 4200ms | [detail](reverse_words_order.md) |
| anagram_check | PASS | 1 | 0 | 5967ms | [detail](anagram_check.md) |
| nth_prime | PASS | 1 | 0 | 7996ms | [detail](nth_prime.md) |
| collatz_steps | PASS | 1 | 0 | 5781ms | [detail](collatz_steps.md) |
| digit_sum_power | PASS | 1 | 0 | 4543ms | [detail](digit_sum_power.md) |
| longest_common_subsequence | PASS | 2 | 0 | 21252ms | [detail](longest_common_subsequence.md) |
| balanced_parens | PASS | 1 | 0 | 7980ms | [detail](balanced_parens.md) |
| tower_of_hanoi | PASS | 1 | 0 | 4346ms | [detail](tower_of_hanoi.md) |

## Summary

All benchmarks passed.

## Aggregate Stats

| Metric | Value |
|--------|-------|
| Pass rate | 100% (32/32) |
| First-try success | 27/32 |
| Total tool calls | 41 |
| Tool errors | 0 |
| Avg tool calls/teaser | 1.3 |
| Total time | 270s |
| Avg time/teaser | 8s |
| Error recovery rate | N/A (no errors) |
