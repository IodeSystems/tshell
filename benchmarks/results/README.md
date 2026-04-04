# Benchmark Results

**Model:** Qwen3-5-35B-A3B-turbo
**Date:** 2026-04-04T04:02:13.308803861
**Score:** 33/33

| Teaser | Status | Tool Calls | Errors | Duration | Details |
|--------|--------|-----------|--------|----------|---------|
| factorial | PASS | 2 | 1 | 26386ms | [detail](Qwen3-5-35B-A3B-turbo/factorial.md) |
| fizzbuzz | PASS | 1 | 0 | 6314ms | [detail](Qwen3-5-35B-A3B-turbo/fizzbuzz.md) |
| closure_counter | PASS | 1 | 0 | 4773ms | [detail](Qwen3-5-35B-A3B-turbo/closure_counter.md) |
| pipe_chain | PASS | 1 | 0 | 4823ms | [detail](Qwen3-5-35B-A3B-turbo/pipe_chain.md) |
| recursive_flatten | PASS | 1 | 0 | 5773ms | [detail](Qwen3-5-35B-A3B-turbo/recursive_flatten.md) |
| object_transform | PASS | 1 | 0 | 5046ms | [detail](Qwen3-5-35B-A3B-turbo/object_transform.md) |
| string_manipulation | PASS | 1 | 0 | 3768ms | [detail](Qwen3-5-35B-A3B-turbo/string_manipulation.md) |
| reduce_groupby | PASS | 1 | 0 | 7760ms | [detail](Qwen3-5-35B-A3B-turbo/reduce_groupby.md) |
| bitwise_flags | PASS | 1 | 0 | 5764ms | [detail](Qwen3-5-35B-A3B-turbo/bitwise_flags.md) |
| scatter_parallel | PASS | 1 | 0 | 3868ms | [detail](Qwen3-5-35B-A3B-turbo/scatter_parallel.md) |
| fibonacci_memo | PASS | 1 | 0 | 4880ms | [detail](Qwen3-5-35B-A3B-turbo/fibonacci_memo.md) |
| regex_extract | PASS | 1 | 0 | 4509ms | [detail](Qwen3-5-35B-A3B-turbo/regex_extract.md) |
| matrix_multiply | PASS | 1 | 0 | 7426ms | [detail](Qwen3-5-35B-A3B-turbo/matrix_multiply.md) |
| deep_clone | PASS | 1 | 0 | 8411ms | [detail](Qwen3-5-35B-A3B-turbo/deep_clone.md) |
| binary_search | PASS | 2 | 1 | 12578ms | [detail](Qwen3-5-35B-A3B-turbo/binary_search.md) |
| curry | PASS | 1 | 0 | 3880ms | [detail](Qwen3-5-35B-A3B-turbo/curry.md) |
| linked_list | PASS | 1 | 0 | 6470ms | [detail](Qwen3-5-35B-A3B-turbo/linked_list.md) |
| pipe_wordfreq | PASS | 3 | 0 | 8833ms | [detail](Qwen3-5-35B-A3B-turbo/pipe_wordfreq.md) |
| roman_numerals | PASS | 2 | 1 | 15000ms | [detail](Qwen3-5-35B-A3B-turbo/roman_numerals.md) |
| merge_sort | PASS | 1 | 0 | 11127ms | [detail](Qwen3-5-35B-A3B-turbo/merge_sort.md) |
| event_emitter | PASS | 1 | 0 | 7637ms | [detail](Qwen3-5-35B-A3B-turbo/event_emitter.md) |
| pipe_csv_parse | PASS | 1 | 0 | 9078ms | [detail](Qwen3-5-35B-A3B-turbo/pipe_csv_parse.md) |
| count_letter_r_strawberry | PASS | 1 | 0 | 3060ms | [detail](Qwen3-5-35B-A3B-turbo/count_letter_r_strawberry.md) |
| count_letter_l_lullaby | PASS | 0 | 0 | 1857ms | [detail](Qwen3-5-35B-A3B-turbo/count_letter_l_lullaby.md) |
| count_words_with_letter | PASS | 1 | 0 | 3741ms | [detail](Qwen3-5-35B-A3B-turbo/count_words_with_letter.md) |
| anagram_check | PASS | 1 | 0 | 3971ms | [detail](Qwen3-5-35B-A3B-turbo/anagram_check.md) |
| nth_prime | PASS | 1 | 0 | 6041ms | [detail](Qwen3-5-35B-A3B-turbo/nth_prime.md) |
| collatz_steps | PASS | 1 | 0 | 5516ms | [detail](Qwen3-5-35B-A3B-turbo/collatz_steps.md) |
| digit_sum_power | PASS | 2 | 1 | 6574ms | [detail](Qwen3-5-35B-A3B-turbo/digit_sum_power.md) |
| longest_common_subsequence | PASS | 1 | 0 | 7791ms | [detail](Qwen3-5-35B-A3B-turbo/longest_common_subsequence.md) |
| balanced_parens | PASS | 1 | 0 | 7120ms | [detail](Qwen3-5-35B-A3B-turbo/balanced_parens.md) |
| tower_of_hanoi | PASS | 1 | 0 | 3175ms | [detail](Qwen3-5-35B-A3B-turbo/tower_of_hanoi.md) |
| escape_heavy_strings | PASS | 1 | 0 | 7665ms | [detail](Qwen3-5-35B-A3B-turbo/escape_heavy_strings.md) |

## Summary

All benchmarks passed.

## Aggregate Stats

| Metric | Value |
|--------|-------|
| Pass rate | 100% (33/33) |
| First-try success | 27/33 |
| Total tool calls | 38 |
| Tool errors | 4 |
| Avg tool calls/teaser | 1.2 |
| Total time | 230s |
| Avg time/teaser | 6s |
| Error recovery rate | 4/4 recovered |
