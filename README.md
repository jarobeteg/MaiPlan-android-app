# MaiPlan Frontend Design Document 🦖

| Author           | Date Created     | Last Edited      | Version | Reviewed By |
|:----------------:|:----------------:|:----------------:|:-------:|:-----------:|
| jarobeteg (Jaro) | November 6, 2025 | November 9, 2025 | 0.1     |-------------|

## Overview 📋
The purpose of this document is to explain the objectives of the project, provide a detailed overview of its technical challanges and their solutions. Furthermore to provide insight into future plans and upcoming implementations. This project represents both a personal learning journey and the realization of my girlfriend's dream project. It is an exploration of different areas within software engineering, where I experiment, overengineer, and get lost in various concepts purely for the sake of learning. Additionally, any ideas or suggestions she brings forward, as well as changes she envisions, are considered an integral part of the project's scope and ongoing development.

## What is the MaiPlan project? 📱
MaiPlan is an Android application to serve as an all in one personal organizer. Combining a daily planner, finance tracker, file manager, note taking tool (with extended list functionality), reminder system, event scheduler, and health tracker. These are the core features planned for the project. The scope will likely expand over time. Future ideas include a shared calendar, a dream wall, a book section (for personal research), and widgets.

## The Frontend 💻
The Android application serves as the frontend of this project. It is developed in Kotlin with the UI built using Jetpack Compose. The app follows the MVVM architecture and an offline-first approach, featuring a local Room database that periodically synchronizes data with the backend server.
