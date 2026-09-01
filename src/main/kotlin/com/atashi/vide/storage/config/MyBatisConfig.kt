package com.atashi.vide.storage.config

import org.mybatis.spring.annotation.MapperScan
import org.springframework.context.annotation.Configuration

@Configuration
@MapperScan("com.atashi.vide.storage.dao")
class MyBatisConfig
