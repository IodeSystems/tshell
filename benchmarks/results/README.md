# Benchmark Results

**Model:** Qwen3-5-35B-A3B
**Date:** 2026-03-15T01:45:00.518463793
**Score:** 32/32

| Teaser | Status | Tool Calls | Errors | Duration | Details |
|--------|--------|-----------|--------|----------|---------|
| factorial | PASS | 1 | 0 | 3911ms | [detail](factorial.md) |
| fizzbuzz | PASS | 1 | 0 | 6602ms | [detail](fizzbuzz.md) |
| closure_counter | PASS | 1 | 0 | 5315ms | [detail](closure_counter.md) |
| pipe_chain | PASS | 1 | 0 | 4736ms | [detail](pipe_chain.md) |
| recursive_flatten | PASS | 1 | 0 | 6821ms | [detail](recursive_flatten.md) |
| object_transform | PASS | 1 | 0 | 5269ms | [detail](object_transform.md) |
| string_manipulation | PASS | 1 | 0 | 3996ms | [detail](string_manipulation.md) |
| reduce_groupby | PASS | 1 | 0 | 7622ms | [detail](reduce_groupby.md) |
| bitwise_flags | PASS | 1 | 0 | 5513ms | [detail](bitwise_flags.md) |
| scatter_parallel | PASS | 1 | 0 | 4131ms | [detail](scatter_parallel.md) |
| fibonacci_memo | PASS | 3 | 0 | 12996ms | [detail](fibonacci_memo.md) |
| regex_extract | PASS | 8 | 0 | 26374ms | [detail](regex_extract.md) |
| matrix_multiply | PASS | 1 | 0 | 8366ms | [detail](matrix_multiply.md) |
| deep_clone | PASS | 1 | 0 | 9964ms | [detail](deep_clone.md) |
| binary_search | PASS | 2 | 0 | 13728ms | [detail](binary_search.md) |
| curry | PASS | 10 | 0 | 27595ms | [detail](curry.md) |
| linked_list | PASS | 1 | 0 | 6194ms | [detail](linked_list.md) |
| pipe_wordfreq | PASS | 1 | 0 | 5104ms | [detail](pipe_wordfreq.md) |
| roman_numerals | PASS | 1 | 0 | 10210ms | [detail](roman_numerals.md) |
| merge_sort | PASS | 1 | 0 | 12531ms | [detail](merge_sort.md) |
| event_emitter | PASS | 1 | 0 | 8382ms | [detail](event_emitter.md) |
| pipe_csv_parse | PASS | 2 | 0 | 14101ms | [detail](pipe_csv_parse.md) |
| count_letter_r_strawberry | PASS | 1 | 0 | 3364ms | [detail](count_letter_r_strawberry.md) |
| count_letter_l_lullaby | PASS | 3 | 0 | 7687ms | [detail](count_letter_l_lullaby.md) |
| count_words_with_letter | PASS | 2 | 0 | 6498ms | [detail](count_words_with_letter.md) |
| anagram_check | PASS | 1 | 0 | 4814ms | [detail](anagram_check.md) |
| nth_prime | PASS | 1 | 0 | 8042ms | [detail](nth_prime.md) |
| collatz_steps | PASS | 1 | 0 | 5741ms | [detail](collatz_steps.md) |
| digit_sum_power | PASS | 1 | 0 | 4657ms | [detail](digit_sum_power.md) |
| longest_common_subsequence | PASS | 2 | 0 | 18310ms | [detail](longest_common_subsequence.md) |
| balanced_parens | PASS | 1 | 0 | 7257ms | [detail](balanced_parens.md) |
| tower_of_hanoi | PASS | 2 | 0 | 5964ms | [detail](tower_of_hanoi.md) |

## Summary

All benchmarks passed.

## Aggregate Stats

| Metric | Value |
|--------|-------|
| Pass rate | 100% (32/32) |
| First-try success | 23/32 |
| Total tool calls | 57 |
| Tool errors | 0 |
| Avg tool calls/teaser | 1.8 |
| Total time | 281s |
| Avg time/teaser | 8s |
| Error recovery rate | N/A (no errors) |
