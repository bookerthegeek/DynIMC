package com.github.lunatrius.imc;

import com.github.lunatrius.imc.deserializer.ItemStackDeserializer;
import com.github.lunatrius.imc.lib.Reference;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

@Mod(modid = Reference.MODID, name = Reference.NAME)
public class DynIMC {
	@Instance(Reference.MODID)
	public static DynIMC instance;

	private Gson gson;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		Reference.logger = event.getModLog();

		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(ItemStack.class, new ItemStackDeserializer());
		this.gson = gsonBuilder.create();

		ModIMC modIMC = readFile(event.getSuggestedConfigurationFile());

		if (modIMC != null) {
			if (modIMC.stringItemStackMap != null) {
				for (Map.Entry<String, ItemStack[]> entry : modIMC.stringItemStackMap.entrySet()) {
					for (ItemStack itemStack : entry.getValue()) {
						FMLInterModComms.sendMessage(modIMC.modid, entry.getKey(), itemStack);
					}
				}
			}

			if (modIMC.stringStringMap != null) {
				for (Map.Entry<String, String[]> entry : modIMC.stringStringMap.entrySet()) {
					for (String string : entry.getValue()) {
						FMLInterModComms.sendMessage(modIMC.modid, entry.getKey(), string);
					}
				}
			}

			if (modIMC.stringNBTTagCompoundMap != null) {
				for (Map.Entry<String, NBTTagCompound[]> entry : modIMC.stringNBTTagCompoundMap.entrySet()) {
					for (NBTTagCompound nbt : entry.getValue()) {
						FMLInterModComms.sendMessage(modIMC.modid, entry.getKey(), nbt);
					}
				}
			}
		}
	}

	private ModIMC readFile(File file) {
		BufferedReader buffer = null;
		try {
			if (file.getParentFile() != null) {
				if (!file.getParentFile().mkdirs()) {
					Reference.logger.debug("Could not create directory!");
				}
			}

			if (!file.exists() && !file.createNewFile()) {
				return null;
			}

			if (file.canRead()) {
				FileReader fileInputStream = new FileReader(file);
				buffer = new BufferedReader(fileInputStream);

				String str = "";

				String line;
				while ((line = buffer.readLine()) != null) {
					str += line + "\n";
				}

				return this.gson.fromJson(str, ModIMC.class);
			}
		} catch (IOException e) {
			Reference.logger.error("IO failure!", e);
		} catch (JsonSyntaxException e) {
			Reference.logger.error(String.format("Malformed JSON in %s!", file.getName()), e);
		} finally {
			if (buffer != null) {
				try {
					buffer.close();
				} catch (IOException e) {
					Reference.logger.error("IO failure!", e);
				}
			}
		}

		return null;
	}

	// TODO: remove when done testing
	@Deprecated
	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		// System.exit(9001);
	}

	// TODO: remove when done testing
	@Deprecated
	@EventHandler
	public void imcCallback(FMLInterModComms.IMCEvent event) {
		for (final FMLInterModComms.IMCMessage imcMessage : event.getMessages()) {
			if (imcMessage.isStringMessage()) {
				Reference.logger.info(imcMessage.getSender() + " sent (string): " + imcMessage.key + " => " + imcMessage.getStringValue());
			} else if (imcMessage.isItemStackMessage()) {
				Reference.logger.info(imcMessage.getSender() + " sent (itemstack): " + imcMessage.key + " => " + imcMessage.getItemStackValue());
			} else if (imcMessage.isNBTMessage()) {
				Reference.logger.info(imcMessage.getSender() + " sent (nbt): " + imcMessage.key + " => " + imcMessage.getNBTValue());
			}
		}
	}
}