/*
 * Copyright 2002-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.util;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;
import java.util.UUID;

/**
 * An {@link IdGenerator} that uses {@link SecureRandom} for the initial seed and
 * {@link Random} thereafter, instead of calling {@link UUID#randomUUID()} every
 * time as {@link org.springframework.util.JdkIdGenerator JdkIdGenerator} does.
 * This provides a better balance between securely random ids and performance.
 *
 * @author Rossen Stoyanchev
 * @author Rob Winch
 * @since 4.0
 */
public class AlternativeJdkIdGenerator implements IdGenerator {

	private final Random random;

	//#> 使用secureRandom生成的随机数作为seed, 生辰一个random, 使用这个random生成uuid

	public AlternativeJdkIdGenerator() {
		//#> 使用SecureRandom生成一个种子
		SecureRandom secureRandom = new SecureRandom();
		byte[] seed = new byte[8];
		secureRandom.nextBytes(seed);
		//#> 使用上面生成的种子, new一个Random.
		this.random = new Random(new BigInteger(seed).longValue());
	}


	@Override
	public UUID generateId() {
		byte[] randomBytes = new byte[16];
		this.random.nextBytes(randomBytes);

		//#> 将生成的随机bytes数组转换成两个需要的64位long, 不过jdk1.8的版本有直接从bytes生成random的方法, 跟这段代码的逻辑一样
		long mostSigBits = 0;
		for (int i = 0; i < 8; i++) {
			mostSigBits = (mostSigBits << 8) | (randomBytes[i] & 0xff);
		}

		long leastSigBits = 0;
		for (int i = 8; i < 16; i++) {
			leastSigBits = (leastSigBits << 8) | (randomBytes[i] & 0xff);
		}

		//#> 使用两个long, 生成一个uuid. 而不是使用默认的UUID layout, 和一些时间相关的东西, 不过生成uuid基本上都是使用randomUUID, 所以uuid的layout就不遵循默认的layout了
		//#> 原来如此: 其实UUID就是两个long型的包装
		return new UUID(mostSigBits, leastSigBits);
	}

}
