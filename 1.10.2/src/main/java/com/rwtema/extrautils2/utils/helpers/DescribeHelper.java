package com.rwtema.extrautils2.utils.helpers;

import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class DescribeHelper {
	public static void addDescription(List<String> list, String name, Object object) {
		addDescription(list, name + " = ");
		addDescription(list, object, 1);
	}

	public static void addDescription(List<String> list, Object object) {
		addDescription(list, object, 0);
	}

	public static void addDescription(List<String> list, Object object, int i) {
		if (object == null) list.add("[null]");
		else if (object instanceof String) {
			StringBuilder builder = new StringBuilder();
			for (int j = 0; j < i; j++) {
				builder.append('\t');
			}
			builder.append(object);
			list.add(builder.toString());
		} else if (object instanceof Iterable) {
			if (object instanceof Collection)
				addDescription(list, object.getClass().getSimpleName() + "_(size = " + ((Collection) object).size() + "){", i);
			else
				addDescription(list, object.getClass().getSimpleName() + " {", i);
			for (Object o : ((Iterable) object)) {
				addDescription(list, o, i + 1);
			}

		} else if (object instanceof Map) {
			addDescription(list, object.getClass().getSimpleName() + "_(size = " + ((Map) object).size() + "){", i);

			for (Map.Entry entry : ((Map<?, ?>) object).entrySet()) {
				addDescription(list, entry.getKey(), i + 1);
				addDescription(list, "=", i + 1);
				addDescription(list, entry.getValue(), i + 1);
			}
			addDescription(list, "}", i);
		} else if (object instanceof ForgeChunkManager.Ticket) {
			ForgeChunkManager.Ticket ticket = (ForgeChunkManager.Ticket) object;
			addDescription(list, "Ticket[Player: " + ticket.getPlayerName() + " World:" + ticket.world.provider.getDimension() + "]", i);
			addDescription(list, ticket.getChunkList().toString(), i);
		} else if (object instanceof World) {
			World world = (World) object;
			addDescription(list, "World[Dim:" + world.provider.getDimension() + " isRemote:" + world.isRemote + "]");
		} else {
			String s = object.toString();
			int hash = object.hashCode();
			String s2 = object.getClass().getName() + "@" + Integer.toHexString(hash);
			if (s.equals(s2)) {
				addDescription(list, object.getClass().getSimpleName() + ":" + hash, i);
			} else {
				addDescription(list, object.getClass().getSimpleName() + ":" + s, i);
			}
		}
	}


}
