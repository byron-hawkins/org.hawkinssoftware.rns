package org.hawkinssoftware.rns.core.log;

public interface LogTagFilter<CategoryType extends Enum<CategoryType>>
{
	boolean includes(LogTag<CategoryType> tag);
}
