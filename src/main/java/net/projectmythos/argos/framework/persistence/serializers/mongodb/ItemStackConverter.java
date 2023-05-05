package net.projectmythos.argos.framework.persistence.serializers.mongodb;

import com.mongodb.BasicDBObject;
import dev.morphia.converters.SimpleValueConverter;
import dev.morphia.converters.TypeConverter;
import dev.morphia.mapping.MappedField;
import dev.morphia.mapping.Mapper;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftItemStack;
import net.projectmythos.argos.utils.SerializationUtils.Json;
import org.bukkit.inventory.ItemStack;

import static net.projectmythos.argos.utils.SerializationUtils.Json.serialize;


public class ItemStackConverter extends TypeConverter implements SimpleValueConverter {

	public ItemStackConverter(Mapper mapper) {
		super(ItemStack.class, CraftItemStack.class);
	}

	@Override
	public Object encode(Object value, MappedField optionalExtraInfo) {
		if (value == null) return null;

		return BasicDBObject.parse(Json.toString(serialize((ItemStack) value)));
	}

	@Override
	public Object decode(Class<?> aClass, Object value, MappedField mappedField) {
		if (value == null) return null;
		return Json.deserializeItemStack(((BasicDBObject) value).toJson());
	}

}
