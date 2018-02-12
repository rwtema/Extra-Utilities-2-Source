package com.rwtema.extrautils2.commands;

import com.google.common.collect.Lists;
import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.achievements.AchievementHelper;
import com.rwtema.extrautils2.chunkloading.XUChunkLoaderManager;
import com.rwtema.extrautils2.power.PowerManager;
import com.rwtema.extrautils2.utils.LogHelper;
import gnu.trove.iterator.TObjectLongIterator;
import gnu.trove.map.hash.TObjectLongHashMap;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;

public class CommandDebug extends CommandBase {
	@Nonnull
	@Override
	public String getName() {
		return "xudebug";
	}

	@Nonnull
	@Override
	public String getUsage(@Nonnull ICommandSender sender) {
		return "xudebug";
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		return ExtraUtils2.deobf || ExtraUtils2.version.contains("fc1-build");
	}

	@Override
	public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) throws CommandException {
		if (args.length == 0) throw new CommandException("Unknown Command");

		String t = args[0];
		List<String> info = Lists.newArrayList();
		switch (t) {
			case "power":
				PowerManager.instance.getDebug(info);
				break;
			case "chunks":
				XUChunkLoaderManager.instance.getDebug(info);
				break;
			case "packethandler":
				break;
			case "onetime":
				info.addAll(LogHelper.getOneTimeStrings());
				break;
			case "ach":
				AchievementHelper.bake();
				break;
			case "tpx":
				break;
			case "checkBlockContents":
				World world = sender.getEntityWorld();
				if (world instanceof WorldServer) {
					TObjectLongHashMap<IBlockState> types = new TObjectLongHashMap<>();
					ChunkProviderServer chunkProvider = ((WorldServer) world).getChunkProvider();
					Collection<Chunk> loadedChunks = chunkProvider.getLoadedChunks();
					long total = 0;
					for (Chunk chunk : loadedChunks) {
						for (int y = 0; y < 256; y++) {
							for (int x = 0; x < 16; x++) {
								for (int z = 0; z < 16; z++) {
									IBlockState blockState = chunk.getBlockState(x, y, z);
									types.adjustOrPutValue(blockState, 1, 1);
								}
							}
						}
					}

					info.add("Block Component Results:");
					TObjectLongIterator<IBlockState> iterator = types.iterator();
					while (iterator.hasNext()) {
						iterator.advance();
						info.add(iterator.key().toString() + ": " + iterator.value() + "  (" + (double) iterator.value() / loadedChunks.size() + ")");
					}
				}

				break;
			default:
				throw new CommandException("Unknown Command");
		}

		for (String s : info) {
			sender.sendMessage(new TextComponentString(s));
		}


	}
}
