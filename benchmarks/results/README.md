# Benchmark Results

**Model:** Qwen3-5-35B-A3B-turbo
**Date:** 2026-04-04T05:22:28.289672568
**Score:** 33/33

| Teaser | Status | Tool Calls | Errors | Duration | Details |
|--------|--------|-----------|--------|----------|---------|
| factorial | PASS | 2 | 1 | 27380ms | [detail](Qwen3-5-35B-A3B-turbo/factorial.md) |
| fizzbuzz | PASS | 2 | 1 | 9676ms | [detail](Qwen3-5-35B-A3B-turbo/fizzbuzz.md) |
| closure_counter | PASS | 0 | 0 | 1859ms | [detail](Qwen3-5-35B-A3B-turbo/closure_counter.md) |
| pipe_chain | PASS | 1 | 0 | 4732ms | [detail](Qwen3-5-35B-A3B-turbo/pipe_chain.md) |
| recursive_flatten | PASS | 1 | 0 | 5842ms | [detail](Qwen3-5-35B-A3B-turbo/recursive_flatten.md) |
| object_transform | PASS | 1 | 0 | 5293ms | [detail](Qwen3-5-35B-A3B-turbo/object_transform.md) |
| string_manipulation | PASS | 1 | 0 | 3915ms | [detail](Qwen3-5-35B-A3B-turbo/string_manipulation.md) |
| reduce_groupby | PASS | 2 | 1 | 12420ms | [detail](Qwen3-5-35B-A3B-turbo/reduce_groupby.md) |
| bitwise_flags | PASS | 1 | 0 | 5460ms | [detail](Qwen3-5-35B-A3B-turbo/bitwise_flags.md) |
| scatter_parallel | PASS | 0 | 0 | 1857ms | [detail](Qwen3-5-35B-A3B-turbo/scatter_parallel.md) |
| fibonacci_memo | PASS | 1 | 0 | 6658ms | [detail](Qwen3-5-35B-A3B-turbo/fibonacci_memo.md) |
| regex_extract | PASS | 2 | 1 | 6554ms | [detail](Qwen3-5-35B-A3B-turbo/regex_extract.md) |
| matrix_multiply | PASS | 1 | 0 | 8335ms | [detail](Qwen3-5-35B-A3B-turbo/matrix_multiply.md) |
| deep_clone | PASS | 1 | 0 | 9040ms | [detail](Qwen3-5-35B-A3B-turbo/deep_clone.md) |
| binary_search | PASS | 2 | 1 | 12604ms | [detail](Qwen3-5-35B-A3B-turbo/binary_search.md) |
| curry | PASS | 1 | 0 | 3780ms | [detail](Qwen3-5-35B-A3B-turbo/curry.md) |
| linked_list | PASS | 1 | 0 | 6083ms | [detail](Qwen3-5-35B-A3B-turbo/linked_list.md) |
| pipe_wordfreq | PASS | 1 | 0 | 4506ms | [detail](Qwen3-5-35B-A3B-turbo/pipe_wordfreq.md) |
| roman_numerals | PASS | 1 | 0 | 10293ms | [detail](Qwen3-5-35B-A3B-turbo/roman_numerals.md) |
| merge_sort | PASS | 1 | 0 | 10761ms | [detail](Qwen3-5-35B-A3B-turbo/merge_sort.md) |
| event_emitter | PASS | 1 | 0 | 6340ms | [detail](Qwen3-5-35B-A3B-turbo/event_emitter.md) |
| pipe_csv_parse | PASS | 1 | 0 | 8436ms | [detail](Qwen3-5-35B-A3B-turbo/pipe_csv_parse.md) |
| count_letter_r_strawberry | PASS | 2 | 1 | 5925ms | [detail](Qwen3-5-35B-A3B-turbo/count_letter_r_strawberry.md) |
| count_letter_l_lullaby | PASS | 1 | 0 | 3185ms | [detail](Qwen3-5-35B-A3B-turbo/count_letter_l_lullaby.md) |
| count_words_with_letter | PASS | 1 | 0 | 3559ms | [detail](Qwen3-5-35B-A3B-turbo/count_words_with_letter.md) |
| anagram_check | PASS | 1 | 0 | 4531ms | [detail](Qwen3-5-35B-A3B-turbo/anagram_check.md) |
| nth_prime | PASS | 0 | 0 | 1760ms | [detail](Qwen3-5-35B-A3B-turbo/nth_prime.md) |
| collatz_steps | PASS | 1 | 0 | 5592ms | [detail](Qwen3-5-35B-A3B-turbo/collatz_steps.md) |
| digit_sum_power | PASS | 1 | 0 | 4137ms | [detail](Qwen3-5-35B-A3B-turbo/digit_sum_power.md) |
| longest_common_subsequence | PASS | 1 | 0 | 8337ms | [detail](Qwen3-5-35B-A3B-turbo/longest_common_subsequence.md) |
| balanced_parens | PASS | 1 | 0 | 7488ms | [detail](Qwen3-5-35B-A3B-turbo/balanced_parens.md) |
| tower_of_hanoi | PASS | 1 | 0 | 3130ms | [detail](Qwen3-5-35B-A3B-turbo/tower_of_hanoi.md) |
| escape_heavy_strings | PASS | 1 | 0 | 6956ms | [detail](Qwen3-5-35B-A3B-turbo/escape_heavy_strings.md) |

## Summary

All benchmarks passed.

## Aggregate Stats

| Metric | Value |
|--------|-------|
| Pass rate | 100% (33/33) |
| First-try success | 24/33 |
| Total tool calls | 36 |
| Tool errors | 6 |
| Avg tool calls/teaser | 1.1 |
| Total time | 226s |
| Avg time/teaser | 6s |
| Error recovery rate | 6/6 recovered |
