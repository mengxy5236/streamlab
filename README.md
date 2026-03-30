# StreamLab

StreamLab is a distributed video platform prototype built with **Spring Boot**, inspired by Bilibili. It focuses on video upload, processing, streaming, and search in high-concurrency scenarios.

## Features

- Video upload and storage
- Video transcoding using FFmpeg
- HLS video streaming support
- Asynchronous task handling with Kafka
- Video search using Elasticsearch
- Caching with Redis for metadata retrieval

## Architecture

- Spring Boot microservices
- Kafka message queue for task decoupling
- Redis caching layer
- Elasticsearch indexing
- OSS and FFmpeg integration for video processing