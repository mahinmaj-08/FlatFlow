# Spike: equal split with leftover-cent rule

Branch: spike/cent-split

## Goal
Find out whether an equal split in integer cents can always add up exactly to the
expense total, and what rule decides who gets the leftover cents.

## Run it
    javac *.java
    java EqualSplitterTest

Needs a JDK (Java 17 or newer, because of the record in Share.java).

## Files
- Share.java: a user id and an amount in cents
- EqualSplitter.java: the split logic
- EqualSplitterTest.java: plain-Java checks, no libraries
