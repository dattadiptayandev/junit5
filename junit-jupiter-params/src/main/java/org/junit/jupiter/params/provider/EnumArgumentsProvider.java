/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.params.provider;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toSet;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ContainerExtensionContext;
import org.junit.jupiter.params.support.AnnotationConsumer;
import org.junit.platform.commons.util.Preconditions;

/**
 * @since 5.0
 */
class EnumArgumentsProvider implements ArgumentsProvider, AnnotationConsumer<EnumSource> {

	private Class<? extends Enum<?>> enumClass;
	private Set<String> names = Collections.emptySet();
	private EnumSource.ConstantSelectionMode mode;

	@Override
	@SuppressWarnings("rawtypes")
	public void accept(EnumSource enumSource) {
		enumClass = enumSource.value();
		mode = enumSource.mode();
		if (enumSource.names().length > 0) {

			names = stream(enumSource.names()).collect(toSet());
			Preconditions.condition(names.size() == enumSource.names().length,
				() -> "Duplicate enum constant name(s) found in annotation: " + enumSource);

			if (mode.isConstantNameExpected()) {
				Set<String> allSet = stream(enumClass.getEnumConstants()).map(Enum::name).collect(toSet());
				Preconditions.condition(allSet.containsAll(names),
					() -> "Invalid enum constant name(s) found in annotation: " + enumSource + ". Valid names are: "
							+ allSet);
			}
		}
	}

	@Override
	public Stream<? extends Arguments> provideArguments(ContainerExtensionContext context) {
		Stream<? extends Enum<?>> stream = stream(enumClass.getEnumConstants());
		if (names != Collections.EMPTY_SET) {
			stream = stream.filter(constant -> mode.select(constant, names));
		}
		return stream.map(ObjectArrayArguments::arguments);
	}

}
