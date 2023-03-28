package io.extremum.common.service;

import io.extremum.common.exceptions.CommonException;

import java.util.function.Consumer;

/**
 * @author rpuch
 */
public interface Problems extends Consumer<CommonException> {
}
