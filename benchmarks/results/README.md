# Benchmark Results

**Model:** Qwen3-5-35B-A3B
**Date:** 2026-03-15T00:38:45.405604564
**Score:** 32/32

| Teaser | Status | Tool Calls | Errors | Duration | Details |
|--------|--------|-----------|--------|----------|---------|
| factorial | PASS | 1 | 0 | 19911ms | [detail](factorial.md) |
| fizzbuzz | PASS | 2 | 0 | 12043ms | [detail](fizzbuzz.md) |
| closure_counter | PASS | 1 | 0 | 5315ms | [detail](closure_counter.md) |
| pipe_chain | PASS | 1 | 0 | 4795ms | [detail](pipe_chain.md) |
| recursive_flatten | PASS | 1 | 0 | 7023ms | [detail](recursive_flatten.md) |
| object_transform | PASS | 1 | 0 | 5237ms | [detail](object_transform.md) |
| string_manipulation | PASS | 1 | 0 | 4436ms | [detail](string_manipulation.md) |
| reduce_groupby | PASS | 3 | 0 | 17445ms | [detail](reduce_groupby.md) |
| bitwise_flags | PASS | 1 | 0 | 5717ms | [detail](bitwise_flags.md) |
| scatter_parallel | PASS | 1 | 0 | 4155ms | [detail](scatter_parallel.md) |
| fibonacci_memo | PASS | 3 | 0 | 15015ms | [detail](fibonacci_memo.md) |
| regex_extract | PASS | 2 | 0 | 8253ms | [detail](regex_extract.md) |
| matrix_multiply | PASS | 1 | 0 | 8788ms | [detail](matrix_multiply.md) |
| deep_clone | PASS | 1 | 0 | 10398ms | [detail](deep_clone.md) |
| binary_search | PASS | 2 | 0 | 14115ms | [detail](binary_search.md) |
| curry | PASS | 4 | 0 | 14182ms | [detail](curry.md) |
| linked_list | PASS | 1 | 0 | 6794ms | [detail](linked_list.md) |
| pipe_wordfreq | PASS | 1 | 0 | 5015ms | [detail](pipe_wordfreq.md) |
| roman_numerals | PASS | 1 | 0 | 10293ms | [detail](roman_numerals.md) |
| merge_sort | PASS | 1 | 0 | 13351ms | [detail](merge_sort.md) |
| event_emitter | PASS | 1 | 0 | 9135ms | [detail](event_emitter.md) |
| pipe_csv_parse | PASS | 2 | 0 | 14445ms | [detail](pipe_csv_parse.md) |
| count_letter_r_strawberry | PASS | 1 | 0 | 3443ms | [detail](count_letter_r_strawberry.md) |
| count_letter_l_lullaby | PASS | 1 | 0 | 3384ms | [detail](count_letter_l_lullaby.md) |
| reverse_words_order | PASS | 1 | 0 | 4102ms | [detail](reverse_words_order.md) |
| anagram_check | PASS | 1 | 0 | 4783ms | [detail](anagram_check.md) |
| nth_prime | PASS | 2 | 0 | 15294ms | [detail](nth_prime.md) |
| collatz_steps | PASS | 1 | 0 | 5834ms | [detail](collatz_steps.md) |
| digit_sum_power | PASS | 1 | 0 | 4369ms | [detail](digit_sum_power.md) |
| longest_common_subsequence | PASS | 2 | 0 | 18342ms | [detail](longest_common_subsequence.md) |
| balanced_parens | PASS | 1 | 0 | 7213ms | [detail](balanced_parens.md) |
| tower_of_hanoi | PASS | 1 | 0 | 3459ms | [detail](tower_of_hanoi.md) |

## Summary

All benchmarks passed.

## Aggregate Stats

| Metric | Value |
|--------|-------|
| Pass rate | 100% (32/32) |
| First-try success | 23/32 |
| Total tool calls | 45 |
| Tool errors | 0 |
| Avg tool calls/teaser | 1.4 |
| Total time | 286s |
| Avg time/teaser | 8s |
| Error recovery rate | N/A (no errors) |
