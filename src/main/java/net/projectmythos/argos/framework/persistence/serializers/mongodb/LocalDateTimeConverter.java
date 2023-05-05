package net.projectmythos.argos.framework.persistence.serializers.mongodb;

import dev.morphia.converters.SimpleValueConverter;
import dev.morphia.converters.TypeConverter;
import dev.morphia.mapping.MappedField;
import dev.morphia.mapping.Mapper;
import lombok.NoArgsConstructor;
import net.projectmythos.argos.utils.Nullables;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@NoArgsConstructor
public class LocalDateTimeConverter extends TypeConverter implements SimpleValueConverter {
	static final String PATTERN = LocalDateConverter.PATTERN + " " + LocalTimeConverter.PATTERN;

	public LocalDateTimeConverter(Mapper mapper) {
		super(LocalDateTime.class);
	}

	@Override
	public Object encode(Object value, MappedField optionalExtraInfo) {
		if (!(value instanceof LocalDateTime)) return null;
		return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format((LocalDateTime) value);
	}

	@Override
	public Object decode(Class<?> aClass, Object value, MappedField mappedField) {
		return decode(value);
	}

	public LocalDateTime decode(Object value) {
		if (!(value instanceof String string)) return null;
		if (Nullables.isNullOrEmpty(string)) return null;

		try {
			return LocalDateTime.parse(string);
		} catch (DateTimeParseException ex) {
			return LocalDateTime.parse(string, DateTimeFormatter.ofPattern(PATTERN));
		}
	}

}
