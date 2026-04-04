# Benchmark Results

**Model:** Qwen3-5-35B-A3B-turbo
**Date:** 2026-04-04T05:25:57.623687669
**Score:** 33/33

| Teaser | Status | Tool Calls | Errors | Duration | Details |
|--------|--------|-----------|--------|----------|---------|
| factorial | PASS | 0 | 0 | 4953ms | [detail](Qwen3-5-35B-A3B-turbo/factorial.md) |
| fizzbuzz | PASS | 1 | 0 | 6517ms | [detail](Qwen3-5-35B-A3B-turbo/fizzbuzz.md) |
| closure_counter | PASS | 0 | 0 | 1736ms | [detail](Qwen3-5-35B-A3B-turbo/closure_counter.md) |
| pipe_chain | PASS | 1 | 0 | 4938ms | [detail](Qwen3-5-35B-A3B-turbo/pipe_chain.md) |
| recursive_flatten | PASS | 1 | 0 | 5781ms | [detail](Qwen3-5-35B-A3B-turbo/recursive_flatten.md) |
| object_transform | PASS | 2 | 1 | 8431ms | [detail](Qwen3-5-35B-A3B-turbo/object_transform.md) |
| string_manipulation | PASS | 1 | 0 | 3751ms | [detail](Qwen3-5-35B-A3B-turbo/string_manipulation.md) |
| reduce_groupby | PASS | 2 | 1 | 12019ms | [detail](Qwen3-5-35B-A3B-turbo/reduce_groupby.md) |
| bitwise_flags | PASS | 1 | 0 | 5767ms | [detail](Qwen3-5-35B-A3B-turbo/bitwise_flags.md) |
| scatter_parallel | PASS | 1 | 0 | 3803ms | [detail](Qwen3-5-35B-A3B-turbo/scatter_parallel.md) |
| fibonacci_memo | PASS | 1 | 0 | 5101ms | [detail](Qwen3-5-35B-A3B-turbo/fibonacci_memo.md) |
| regex_extract | PASS | 2 | 1 | 6654ms | [detail](Qwen3-5-35B-A3B-turbo/regex_extract.md) |
| matrix_multiply | PASS | 1 | 0 | 7662ms | [detail](Qwen3-5-35B-A3B-turbo/matrix_multiply.md) |
| deep_clone | PASS | 1 | 0 | 9154ms | [detail](Qwen3-5-35B-A3B-turbo/deep_clone.md) |
| binary_search | PASS | 2 | 0 | 11938ms | [detail](Qwen3-5-35B-A3B-turbo/binary_search.md) |
| curry | PASS | 1 | 0 | 3689ms | [detail](Qwen3-5-35B-A3B-turbo/curry.md) |
| linked_list | PASS | 1 | 0 | 6014ms | [detail](Qwen3-5-35B-A3B-turbo/linked_list.md) |
| pipe_wordfreq | PASS | 1 | 0 | 4857ms | [detail](Qwen3-5-35B-A3B-turbo/pipe_wordfreq.md) |
| roman_numerals | PASS | 1 | 0 | 8711ms | [detail](Qwen3-5-35B-A3B-turbo/roman_numerals.md) |
| merge_sort | PASS | 1 | 0 | 11687ms | [detail](Qwen3-5-35B-A3B-turbo/merge_sort.md) |
| event_emitter | PASS | 1 | 0 | 6337ms | [detail](Qwen3-5-35B-A3B-turbo/event_emitter.md) |
| pipe_csv_parse | PASS | 1 | 0 | 7980ms | [detail](Qwen3-5-35B-A3B-turbo/pipe_csv_parse.md) |
| count_letter_r_strawberry | PASS | 1 | 0 | 3074ms | [detail](Qwen3-5-35B-A3B-turbo/count_letter_r_strawberry.md) |
| count_letter_l_lullaby | PASS | 3 | 2 | 7507ms | [detail](Qwen3-5-35B-A3B-turbo/count_letter_l_lullaby.md) |
| count_words_with_letter | PASS | 1 | 0 | 3673ms | [detail](Qwen3-5-35B-A3B-turbo/count_words_with_letter.md) |
| anagram_check | PASS | 1 | 0 | 4126ms | [detail](Qwen3-5-35B-A3B-turbo/anagram_check.md) |
| nth_prime | PASS | 1 | 0 | 5943ms | [detail](Qwen3-5-35B-A3B-turbo/nth_prime.md) |
| collatz_steps | PASS | 1 | 0 | 5922ms | [detail](Qwen3-5-35B-A3B-turbo/collatz_steps.md) |
| digit_sum_power | PASS | 1 | 0 | 3928ms | [detail](Qwen3-5-35B-A3B-turbo/digit_sum_power.md) |
| longest_common_subsequence | PASS | 1 | 0 | 8935ms | [detail](Qwen3-5-35B-A3B-turbo/longest_common_subsequence.md) |
| balanced_parens | PASS | 1 | 0 | 7158ms | [detail](Qwen3-5-35B-A3B-turbo/balanced_parens.md) |
| tower_of_hanoi | PASS | 1 | 0 | 2983ms | [detail](Qwen3-5-35B-A3B-turbo/tower_of_hanoi.md) |
| escape_heavy_strings | PASS | 1 | 0 | 7287ms | [detail](Qwen3-5-35B-A3B-turbo/escape_heavy_strings.md) |

## Summary

All benchmarks passed.

## Aggregate Stats

| Metric | Value |
|--------|-------|
| Pass rate | 100% (33/33) |
| First-try success | 26/33 |
| Total tool calls | 37 |
| Tool errors | 5 |
| Avg tool calls/teaser | 1.1 |
| Total time | 208s |
| Avg time/teaser | 6s |
| Error recovery rate | 4/5 recovered |
