Tuning method
=============

From version 1.8.1, we are trying evaluation parameters optimization with Texel's Tuning Method:

https://www.chessprogramming.org/Texel's_Tuning_Method

There is a script using CuteChess to generate the file with the 64000 games:

```
./scripts/games_for_tuning.sh
```

It outputs a /tmp/games_64000.pgn

Then PositionsGenerator generates the file with the positions from this PGN file.
It includes only "quiet" positions, where the score from the qSearch is the same as the evaluation score.
So later we need to call only the evaluation function to tune the parameters, speeding up the process.

We also manually remove duplicated positions with:

```
cat /tmp/tune_positions_in.txt | sort | uniq > /tmp/tune_positions.txt
```