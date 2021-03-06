package com.kosmx.emotecraft.config;

import com.google.gson.*;
import com.kosmx.emotecraft.Emote;
import com.kosmx.emotecraft.Main;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.apache.logging.log4j.Level;

import java.lang.reflect.Type;


public class EmoteSerializer implements JsonDeserializer<EmoteHolder>, JsonSerializer<EmoteHolder> {

    //Todo create error feedback about missing items (names)
    @Override
    public EmoteHolder deserialize(JsonElement p, Type typeOf, JsonDeserializationContext ctxt) throws JsonParseException{
        JsonObject node = p.getAsJsonObject();
        MutableText author = (MutableText) LiteralText.EMPTY;
        MutableText name = Text.Serializer.fromJson(node.get("name"));
        if(node.has("author")){
            author = Text.Serializer.fromJson(node.get("author"));
        }
        MutableText description = (MutableText) LiteralText.EMPTY;
        if(node.has("description")){
            description = (LiteralText) Text.Serializer.fromJson(node.get("description"));
        }
        node.entrySet().forEach((entry)->{
            String string = entry.getKey();
            if(string.equals("author") || string.equals("comment") || string.equals("name") || string.equals("description") || string.equals("emote"))
                return;
            Main.log(Level.WARN, "Can't understadt: " + string + " : " + entry.getValue());
            Main.log(Level.WARN, "If it is a comment, ignore the warning");
        });
        Emote emote = emoteDeserializer(node.getAsJsonObject("emote"));
        return new EmoteHolder(emote, name, description, author, node.hashCode());
    }

    private Emote emoteDeserializer(JsonObject node) throws JsonParseException{
        int beginTick = 0;
        if(node.has("beginTick")){
            beginTick = node.get("beginTick").getAsInt();
        }
        int endTick = node.get("endTick").getAsInt();
        if(endTick <= 0) throw new JsonParseException("endTick must be bigger than 0");
        boolean isLoop = false;
        int returnTick = 0;
        if(node.has("isLoop") && node.has("returnTick")){
            isLoop = node.get("isLoop").getAsBoolean();
            returnTick = node.get("returnTick").getAsInt();
            if(isLoop && (returnTick >= endTick || returnTick < 0))
                throw new JsonParseException("return tick have to be smaller than endTick and not smaller than 0");
        }

        node.entrySet().forEach((entry)->{
            String string = entry.getKey();
            if(string.equals("beginTick") || string.equals("comment") || string.equals("endTick") || string.equals("stopTick") || string.equals("degrees") || string.equals("moves") || string.equals("returnTick") || string.equals("isLoop"))
                return;
            Main.log(Level.WARN, "Can't understadt: " + string + " : " + entry.getValue());
            Main.log(Level.WARN, "If it is a comment, ignore the warning");
        });
        int resetTick = node.has("stopTick") ? node.get("stopTick").getAsInt() : endTick;
        boolean degrees = ! node.has("degrees") || node.get("degrees").getAsBoolean();
        Emote emote = new Emote(beginTick, endTick, resetTick, isLoop, returnTick);
        moveDeserializer(emote, node.getAsJsonArray("moves"), degrees);
        return emote;
    }

    private void moveDeserializer(Emote emote, JsonArray node, boolean degrees){
        for(JsonElement n : node){
            JsonObject obj = n.getAsJsonObject();
            int tick = obj.get("tick").getAsInt();
            String easing = obj.has("easing") ? obj.get("easing").getAsString() : "linear";
            obj.entrySet().forEach((entry)->{
                String string = entry.getKey();
                if(string.equals("tick") || string.equals("comment") || string.equals("easing") || string.equals("turn") || string.equals("head") || string.equals("torso") || string.equals("rightArm") || string.equals("leftArm") || string.equals("rightLeg") || string.equals("leftLeg"))
                    return;
                Main.log(Level.WARN, "Can't understadt: " + string + " : " + entry.getValue());
                Main.log(Level.WARN, "If it is a comment, ignore the warning");
            });
            int turn = obj.has("turn") ? obj.get("turn").getAsInt() : 0;
            addBodyPartIfExists(emote.head, "head", obj, degrees, tick, easing, turn);
            addBodyPartIfExists(emote.torso, "torso", obj, degrees, tick, easing, turn);
            addBodyPartIfExists(emote.rightArm, "rightArm", obj, degrees, tick, easing, turn);
            addBodyPartIfExists(emote.leftArm, "leftArm", obj, degrees, tick, easing, turn);
            addBodyPartIfExists(emote.rightLeg, "rightLeg", obj, degrees, tick, easing, turn);
            addBodyPartIfExists(emote.leftLeg, "leftLeg", obj, degrees, tick, easing, turn);
        }
    }

    private void addBodyPartIfExists(Emote.BodyPart part, String name, JsonObject node, boolean degrees, int tick, String easing, int turn){
        if(node.has(name)){
            JsonObject partNode = node.get(name).getAsJsonObject();
            partNode.entrySet().forEach((entry)->{
                String string = entry.getKey();
                if(string.equals("x") || string.equals("y") || string.equals("z") || string.equals("pitch") || string.equals("yaw") || string.equals("roll") || string.equals("comment") || string.equals("bend") || string.equals("axis"))
                    return;
                Main.log(Level.WARN, "Can't understadt: " + string + " : " + entry.getValue());
                Main.log(Level.WARN, "If it is a comment, ignore the warning");
            });
            addPartIfExists(part.x, "x", partNode, degrees, tick, easing, turn);
            addPartIfExists(part.y, "y", partNode, degrees, tick, easing, turn);
            addPartIfExists(part.z, "z", partNode, degrees, tick, easing, turn);
            addPartIfExists(part.pitch, "pitch", partNode, degrees, tick, easing, turn);
            addPartIfExists(part.yaw, "yaw", partNode, degrees, tick, easing, turn);
            addPartIfExists(part.roll, "roll", partNode, degrees, tick, easing, turn);
            addPartIfExists(part.bend, "bend", partNode, degrees, tick, easing, turn);
            addPartIfExists(part.axis, "axis", partNode, degrees, tick, easing, turn);
        }
    }

    private void addPartIfExists(Emote.Part part, String name, JsonObject node, boolean degrees, int tick, String easing, int turn){
        if(node.has(name)){
            Emote.addMove(part, tick, node.get(name).getAsFloat(), easing, turn, degrees);
        }
    }




    /**
     * To serialize emotes to Json.
     * This code was not used in the mod, but I left it here for modders.
     *
     * If you want to serialize an emote without EmoteHolder
     * do new EmoteHolder(emote, new LiteralText("name").formatted(Formatting.WHITE), new LiteralText("someString").formatted(Formatting.GRAY), new LiteralText("author").formatted(Formatting.GRAY), some random hash(int));
     * (this code is from {@link com.kosmx.quarktool.QuarkReader#getEmote()})
     *
     * or use {@link EmoteSerializer#emoteSerializer(Emote)}
     *
     *
     * @param emote source EmoteHolder
     * @param typeOfSrc idk
     * @param context :)
     * @return :D
     * Sorry for these really... useful comments
     */
    @Override
    public JsonElement serialize(EmoteHolder emote, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject node = new JsonObject();
        node.add("name", Text.Serializer.toJsonTree(emote.name));
        node.add("description", Text.Serializer.toJsonTree(emote.description)); // :D
        if(!emote.author.getString().equals("")){
            node.add("author", Text.Serializer.toJsonTree(emote.author));
        }
        node.add("emote", emoteSerializer(emote.getEmote()));
        return node;
    }

    /**
     * serialize an emote to json
     * It won't be the same json file (not impossible) but multiple jsons can mean the same emote...
     *
     * Oh, and it's public and static, so you can call it from anywhere.
     *
     * @param emote Emote to serialize
     * @return return Json object
     */
    public static JsonObject emoteSerializer(Emote emote){
        JsonObject node = new JsonObject();
        node.addProperty("beginTick", emote.getBeginTick());
        node.addProperty("endTick", emote.getEndTick());
        node.addProperty("stopTick", emote.getStopTick());
        node.addProperty("isLoop", emote.isInfinite());
        node.addProperty("returnTick", emote.getReturnTick());
        node.addProperty("degrees", false); //No program uses degrees.
        node.add("moves", moveSerializer(emote));
        return node;
    }

    public static JsonArray moveSerializer(Emote emote){
        JsonArray node = new JsonArray();
        bodyPartDeserializer(node, emote.head);
        bodyPartDeserializer(node, emote.torso);
        bodyPartDeserializer(node, emote.rightArm);
        bodyPartDeserializer(node, emote.leftArm);
        bodyPartDeserializer(node, emote.rightLeg);
        bodyPartDeserializer(node, emote.leftLeg);
        return node;
    }

    /*
     * from here and below the methods are not public
     * these are really depend on the upper method and I don't think anyone will use these.
     */
    private static void bodyPartDeserializer(JsonArray node, Emote.BodyPart bodyPart){
        partDeserialize(node, bodyPart.x, bodyPart.name);
        partDeserialize(node, bodyPart.y, bodyPart.name);
        partDeserialize(node, bodyPart.z, bodyPart.name);
        partDeserialize(node, bodyPart.pitch, bodyPart.name);
        partDeserialize(node, bodyPart.yaw, bodyPart.name);
        partDeserialize(node, bodyPart.roll, bodyPart.name);
        partDeserialize(node, bodyPart.bend, bodyPart.name);
        partDeserialize(node, bodyPart.axis, bodyPart.name);
    }

    private static void partDeserialize(JsonArray array, Emote.Part part, String parentName){
        for(Emote.Move move : part.getList()){
            JsonObject node = new JsonObject();
            node.addProperty("tick", move.tick);
            node.addProperty("easing", move.getEase());
            JsonObject jsonMove = new JsonObject();
            jsonMove.addProperty(part.name, move.value);
            node.add(parentName, jsonMove);
            array.add(node);
        }
    }
}
