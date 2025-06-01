
        /*
mindustry preoxide lib
            Copyright (C) 2025 EmmmM9O

            This program is free software: you can redistribute it and/or modify
            it under the terms of the GNU General Public License as published by
            the Free Software Foundation, either version 3 of the License, or
            (at your option) any later version.

            This program is distributed in the hope that it will be useful,
            but WITHOUT ANY WARRANTY; without even the implied warranty of
            MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
            GNU General Public License for more details.

            You should have received a copy of the GNU General Public License
            along with this program.  If not, see <https://www.gnu.org/licenses/>.
        */
package preoxide.mod;

import java.lang.reflect.*;

import arc.*;
import arc.files.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.serialization.*;
import arc.util.serialization.Json.*;
import arc.util.serialization.Jval.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.content.TechTree.*;
import mindustry.ctype.*;
import mindustry.game.Objectives.*;
import mindustry.graphics.*;
import mindustry.graphics.g3d.*;
import mindustry.graphics.g3d.PlanetGrid.*;
import mindustry.io.*;
import mindustry.maps.generators.*;
import mindustry.maps.planet.*;
import mindustry.mod.*;
import mindustry.mod.Mods.*;
import mindustry.type.*;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;
import static mindustry.Vars.*;

@SuppressWarnings({"unchecked", "rawtypes"})
public class POContentParser implements ContentParserI {
  public static final boolean ignoreUnknownFields = true;
  public static final ContentType[] typesToSearch = {ContentType.planet};
  public static final String[] fileExtensions = {"json", "json5"};
  ObjectSet<Class<?>> implicitNullable = ObjectSet.with(TextureRegion.class, TextureRegion[].class,
      TextureRegion[][].class, TextureRegion[][][].class);
  public Seq<POParseListener> listeners = new Seq<>();
  ObjectMap<Class<?>, ContentType> contentTypes = new ObjectMap<>();
  LoadedMod currentMod;
  Content currentContent;
  ObjectMap<Class<?>, Seq<POChildParser>> childParsers = new ObjectMap<>() {
    {
      put(GenericMesh.class, Seq.with(

          new POChildParser<GenericMesh, Planet>() {
            public GenericMesh parse(Planet planet, JsonValue data) throws Exception {
              if (data.isArray()) {
                var res = new GenericMesh[data.size];
                for (int i = 0; i < data.size; i++) {
                  res[i] = parse(planet, data.get(i));
                }
                return new MultiMesh(res);
              }
              String tname = Strings.capitalize(data.getString("type", "NoiseMesh"));

              return switch (tname) {
                // TODO NoiseMesh is bad
                case "NoiseMesh" -> new NoiseMesh(planet, data.getInt("seed", 0),
                    data.getInt("divisions", 1), data.getFloat("radius", 1f),
                    data.getInt("octaves", 1), data.getFloat("persistence", 0.5f),
                    data.getFloat("scale", 1f), data.getFloat("mag", 0.5f),
                    Color.valueOf(data.getString("color1", data.getString("color", "ffffff"))),
                    Color.valueOf(data.getString("color2", data.getString("color", "ffffff"))),
                    data.getInt("colorOct", 1), data.getFloat("colorPersistence", 0.5f),
                    data.getFloat("colorScale", 1f), data.getFloat("colorThreshold", 0.5f));
                case "SunMesh" -> {
                  var cvals = data.get("colors").asStringArray();
                  var colors = new Color[cvals.length];
                  for (int i = 0; i < cvals.length; i++) {
                    colors[i] = Color.valueOf(cvals[i]);
                  }

                  yield new SunMesh(planet, data.getInt("divisions", 1), data.getInt("octaves", 1),
                      data.getFloat("persistence", 0.5f), data.getFloat("scl", 1f),
                      data.getFloat("pow", 1f), data.getFloat("mag", 0.5f),
                      data.getFloat("colorScale", 1f), colors);
                }
                case "HexSkyMesh" -> new HexSkyMesh(planet, data.getInt("seed", 0),
                    data.getFloat("speed", 0), data.getFloat("radius", 1f),
                    data.getInt("divisions", 3), Color.valueOf(data.getString("color", "ffffff")),
                    data.getInt("octaves", 1), data.getFloat("persistence", 0.5f),
                    data.getFloat("scale", 1f), data.getFloat("thresh", 0.5f));
                case "MultiMesh" -> new MultiMesh(parse(planet, data.get("meshes")));
                case "MatMesh" -> new MatMesh(parse(planet, data.get("mesh")),
                    parser.readValue(Mat3D.class, data.get("mat")));
                case "HexMesh" -> new HexMesh(planet, data.getInt("divisions", 6));
                default -> throw new RuntimeException("Unknown mesh type: " + tname);
              };
            };
          }));
    }
  };

  public <R, T> void addChildParser(Class<? extends R> clazz, POChildParser<R, T> cParser) {
    childParsers.get(clazz, Seq::new).add(cParser);
  }

  public <R, T> R parseChild(Class<? extends R> clazz, T father, JsonValue value) throws Exception {
    R res = null;
    for (var cparser : ((Seq<POChildParser<R, T>>) (Object) childParsers.getThrow(clazz,
        () -> new ArcRuntimeException("No child parser for " + clazz.toString())))) {
      if ((res = cparser.parse(father, value)) != null)
        return res;
    }
    throw new ArcRuntimeException("No child parser available for " + clazz.toString());
  }

  ObjectMap<Class<?>, POFieldParser> classParsers = new ObjectMap<>() {
    {
      put(Color.class, (type, data) -> Color.valueOf(data.asString()));
      put(PlanetGenerator.class, (type, data) -> {
        if (data.isString()) {
          return make(resolve(PlanetGenerator.class, data.asString()));
        }
        var result = new AsteroidGenerator();
        readFields(result, data);
        return result;
      });
      put(Vec3.class, (type, data) -> {
        if (data.isArray())
          return new Vec3(data.asFloatArray());
        return new Vec3(data.getFloat("x", 0f), data.getFloat("y", 0f), data.getFloat("z", 0f));
      });
    }
  };

  public <T> void addClassParser(Class<T> clazz, POFieldParser<T> cParser) {
    classParsers.put(clazz, cParser);
  }

  public Json parser = new Json() {
    @Override
    public <T> T readValue(Class<T> type, Class elementType, JsonValue jsonData, Class keyType) {
      T t = internalRead(type, elementType, jsonData, keyType);
      if (t != null && !Reflect.isWrapper(t.getClass()) && (type == null || !type.isPrimitive())) {
        checkNullFields(t);
        listeners.each(hook -> hook.parsed(type, jsonData, t));
      }
      return t;
    }

    private <T> T internalRead(Class<T> type, Class elementType, JsonValue jsonData,
        Class keyType) {
      if (type != null) {
        if (classParsers.containsKey(type)) {
          try {
            return (T) classParsers.get(type).parse(type, jsonData);
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        }
        if ((type == int.class || type == Integer.class) && jsonData.isArray()) {
          int value = 0;
          for (var str : jsonData) {
            if (!str.isString())
              throw new SerializationException(
                  "Integer bitfield values must all be strings. Found: " + str);
            String field = str.asString();
            value |= Reflect.<Integer>get(Env.class, field);
          }
          return (T) (Integer) value;
        }
        // try to parse "item/amount" syntax
        if (type == ItemStack.class && jsonData.isString() && jsonData.asString().contains("/")) {
          String[] split = jsonData.asString().split("/");

          return (T) fromJson(ItemStack.class,
              "{item: " + split[0] + ", amount: " + split[1] + "}");
        }

        // try to parse "payloaditem/amount" syntax
        if (type == PayloadStack.class && jsonData.isString()
            && jsonData.asString().contains("/")) {
          String[] split = jsonData.asString().split("/");
          int number = Strings.parseInt(split[1], 1);
          UnlockableContent cont =
              content.unit(split[0]) == null ? content.block(split[0]) : content.unit(split[0]);

          return (T) new PayloadStack(cont == null ? Blocks.router : cont, number);
        }

        // try to parse "liquid/amount" syntax
        if (jsonData.isString() && jsonData.asString().contains("/")) {
          String[] split = jsonData.asString().split("/");
          if (type == LiquidStack.class) {
            return (T) fromJson(LiquidStack.class,
                "{liquid: " + split[0] + ", amount: " + split[1] + "}");
          } else if (type == ConsumeLiquid.class) {
            return (T) fromJson(ConsumeLiquid.class,
                "{liquid: " + split[0] + ", amount: " + split[1] + "}");
          }
        }

        // try to parse Rect as array
        if (type == Rect.class && jsonData.isArray() && jsonData.size == 4) {
          return (T) new Rect(jsonData.get(0).asFloat(), jsonData.get(1).asFloat(),
              jsonData.get(2).asFloat(), jsonData.get(3).asFloat());
        }

        // search across different content types to find one by name
        if (type == UnlockableContent.class) {
          for (ContentType c : typesToSearch) {
            T found = (T) locate(c, jsonData.asString());
            if (found != null) {
              return found;
            }
          }
          throw new IllegalArgumentException("\"" + jsonData.name
              + "\": No content found with name '" + jsonData.asString() + "'.");
        }

        if (Content.class.isAssignableFrom(type)) {
          ContentType ctype = contentTypes.getThrow(type, () -> new IllegalArgumentException(
              "No content type for class: " + type.getSimpleName()));
          String prefix = currentMod != null ? currentMod.name + "-" : "";
          T one = (T) Vars.content.getByName(ctype, prefix + jsonData.asString());
          if (one != null)
            return one;
          T two = (T) Vars.content.getByName(ctype, jsonData.asString());

          if (two != null)
            return two;
          throw new IllegalArgumentException("\"" + jsonData.name + "\": No " + ctype
              + " found with name '" + jsonData.asString() + "'.\nMake sure '" + jsonData.asString()
              + "' is spelled correctly, and that it really exists!\nThis may also occur because its file failed to parse.");
        }

      }
      return super.readValue(type, elementType, jsonData, keyType);
    }
  };
  private Seq<Runnable> reads = new Seq<>();
  private Seq<Runnable> postreads = new Seq<>();
  private ObjectSet<Object> toBeParsed = new ObjectSet<>();
  public ObjectMap<ContentType, Seq<POTypeParserListener>> typeListeners = new ObjectMap<>();

  public <T extends Content> void addTypeListener(ContentType type,
      POTypeParserListener<T> tListener) {
    typeListeners.get(type, Seq::new).add(tListener);
  }

  public <T extends Content> T toggleTypeListeners(ContentType type, T current, String mod,
      String name, JsonValue value) throws Exception {
    for (var tListener : (Seq<POTypeParserListener<T>>) (Object) typeListeners.get(type,
        Seq::new)) {
      current = tListener.parse(current, mod, name, value);
      currentContent = current;
    }
    return current;
  }

  ObjectMap<ContentType, POTypeParser<?>> parsers =
      ObjectMap.of(ContentType.sector, (POTypeParser<SectorPreset>) (mod, name, value) -> {
        if (value.isString()) {
          return locate(ContentType.sector, name);
        }

        if (!value.has("sector") || !value.get("sector").isNumber())
          throw new RuntimeException("SectorPresets must have a sector number.");

        SectorPreset out = new SectorPreset(mod + "-" + name, currentMod);

        currentContent = out;
        read(() -> {
          Planet planet = locate(ContentType.planet, value.getString("planet", "serpulo"));

          if (planet == null)
            throw new RuntimeException("Planet '" + value.getString("planet") + "' not found.");
          var current = out;
          try {
            current = toggleTypeListeners(ContentType.sector, current, mod, name, value);
          } catch (Throwable e) {
            Log.err(e);
          }
          current.initialize(planet, value.getInt("sector", 0));

          value.remove("sector");
          value.remove("planet");

          if (value.has("rules")) {
            JsonValue r = value.remove("rules");
            if (!r.isObject())
              throw new RuntimeException("Rules must be an object!");
            current.rules = rules -> {
              try {
                // Use standard JSON, this is not content-parser relevant
                JsonIO.json.readFields(rules, r);
              } catch (Throwable e) { // Try not to crash here, as that would be catastrophic and
                                      // confusing
                Log.err(e);
              }
            };
          }

          readFields(current, value);
        });
        return out;
      }, ContentType.planet, (POTypeParser<Planet>) (mod, name, value) -> {
        name = value.getString("name", name);
        readDisplayBundle(ContentType.planet, name, value);
        if (value.isString())
          return locate(ContentType.planet, name);

        Planet parent = locate(ContentType.planet, value.getString("parent", ""));
        Planet planet_ =
            make(resolve(ContentType.planet, value.getString("type", "planet"), Planet.class),
                new Class[] {String.class, Planet.class, float.class, int.class}, mod + "-" + name,
                parent, value.getFloat("radius", 1f), value.getInt("sectorSize", 0));
        if (value.has("type"))
          value.remove("type");
        if (value.has("name"))
          value.remove("name");
        if (value.has("parent"))
          value.remove("parent");

        if (value.has("radius"))
          value.remove("radius");

        planet_ =

            toggleTypeListeners(ContentType.planet, planet_, mod, name, value);

        Planet planet = planet_;
        if (planet instanceof CustomizeParser cParser) {
          cParser.parse(name, mod, value);
        }
        if (value.has("mesh")) {
          var mesh = value.get("mesh");
          if (!mesh.isObject() && !mesh.isArray())
            throw new RuntimeException("Meshes must be objects.");
          value.remove("mesh");
          planet.meshLoader = () -> {
            // don't crash, just log an error
            try {
              return parseChild(GenericMesh.class, planet, mesh);
            } catch (Exception e) {
              Log.err(e);
              return new ShaderSphereMesh(planet, Shaders.unlit, 2);
            }
          };
        }

        if (value.has("cloudMesh")) {
          var mesh = value.get("cloudMesh");
          if (!mesh.isObject() && !mesh.isArray())
            throw new RuntimeException("Meshes must be objects.");
          value.remove("cloudMesh");
          planet.cloudMeshLoader = () -> {
            // don't crash, just log an error
            try {
              return parseChild(GenericMesh.class, planet, mesh);
            } catch (Exception e) {
              Log.err(e);
              return null;
            }
          };
        }

        // always one sector right now...
        if (value.has("sectorSize")) {
          planet.sectors.add(new Sector(planet, Ptile.empty));
          value.remove("sectorSize");
        }
        currentContent = planet;

        read(() -> readFields(planet, value));
        return planet;

      });

  public <T extends MappableContent> T locate(ContentType type, String name) {
    T first = Vars.content.getByName(type, name);
    return first != null ? first : Vars.content.getByName(type, currentMod.name + "-" + name);
  }

  private void attempt(Runnable run) {
    try {
      run.run();
    } catch (Throwable t) {
      Log.err(t);
      markError(currentContent, t);
    }
  }

  @Override
  public void finishParsing() {
    reads.each(this::attempt);
    postreads.each(this::attempt);
    reads.clear();
    postreads.clear();
    toBeParsed.clear();
  }

  @Override
  public Content parse(LoadedMod mod, String name, String json, Fi file, ContentType type)
      throws Exception {
    for (var extension : fileExtensions) {
      if (file.extension().equals(extension)) {
        json = json.replace("#", "\\#");
      }
    }
    currentMod = mod;
    JsonValue value = parser.fromJson(null, Jval.read(json).toString(Jformat.plain));

    if (!parsers.containsKey(type)) {
      throw new SerializationException("No preoxide parsers for content type '" + type + "'");
    }
    boolean located = locate(type, name) != null;
    Content c = parsers.get(type).parse(mod.name, name, value);
    c.minfo.sourceFile = file;
    toBeParsed.add(c);

    if (!located) {
      c.minfo.mod = mod;
    }
    return c;
  }

  @Override
  public void addParseListener(POParseListener listener) {
    listeners.add(listener);
  }

  @Override
  public void init() {
    for (ContentType type : ContentType.all) {
      Seq<Content> arr = Vars.content.getBy(type);
      if (!arr.isEmpty()) {
        Class<?> c = arr.first().getClass();

        while (!(c.getSuperclass() == Content.class || c.getSuperclass() == UnlockableContent.class
            || Modifier.isAbstract(c.getSuperclass().getModifiers()))) {
          c = c.getSuperclass();
        }

        contentTypes.put(c, type);
      }
    }
  }

  @Override
  public void markError(Content content, LoadedMod mod, Fi file, Throwable error) {
    Log.err("Preoxide Parser Error for @ / @:\n@\n", content, file, Strings.getStackTrace(error));

    content.minfo.mod = mod;
    content.minfo.sourceFile = file;
    content.minfo.error = makeError(error, file);
    content.minfo.baseError = error;
    if (mod != null) {
      mod.erroredContent.add(content);
    }
  }

  @Override
  public void markError(Content content, Throwable error) {
    if (content.minfo != null && !content.hasErrored()) {
      markError(content, content.minfo.mod, content.minfo.sourceFile, error);
    }
  }

  private String makeError(Throwable t, Fi file) {
    StringBuilder builder = new StringBuilder();
    builder.append("[lightgray]").append("File: ").append(file.name()).append("[]\n\n");

    if (t.getMessage() != null && t instanceof JsonParseException) {
      builder.append("[accent][[JsonParse][] ").append(":\n").append(t.getMessage());
    } else if (t instanceof NullPointerException) {
      builder.append(Strings.neatError(t));
    } else {
      Seq<Throwable> causes = Strings.getCauses(t);
      for (Throwable e : causes) {
        builder.append("[accent][[").append(e.getClass().getSimpleName().replace("Exception", ""))
            .append("][] ")
            .append(e.getMessage() != null
                ? e.getMessage().replace("mindustry.", "").replace("arc.", "")
                : "")
            .append("\n");
      }
    }
    return builder.toString();
  }

  void checkNullFields(Object object) {
    if (object == null || object instanceof Number || object instanceof String
        || toBeParsed.contains(object) || object.getClass().getName().startsWith("arc."))
      return;

    parser.getFields(object.getClass()).values().toSeq().each(field -> {
      try {
        if (field.field.getType().isPrimitive())
          return;

        if (!field.field.isAnnotationPresent(Nullable.class) && field.field.get(object) == null
            && !implicitNullable.contains(field.field.getType())) {
          throw new RuntimeException("'" + field.field.getName() + "' in "
              + ((object.getClass().isAnonymousClass() ? object.getClass().getSuperclass()
                  : object.getClass()).getSimpleName())
              + " is missing! Object = " + object + ", field = (" + field.field.getName() + " = "
              + field.field.get(object) + ")");
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });

  }

  <T extends Content> T find(ContentType type, String name) {
    Content c = Vars.content.getByName(type, name);
    if (c == null)
      c = Vars.content.getByName(type, currentMod.name + "-" + name);
    if (c == null)
      throw new IllegalArgumentException("No " + type + " found with name '" + name + "'");
    return (T) c;
  }

  /** Call to read a content's extra info later. */
  void read(Runnable run) {
    Content cont = currentContent;
    LoadedMod mod = currentMod;
    reads.add(() -> {
      this.currentMod = mod;
      this.currentContent = cont;
      run.run();

      // check nulls after parsing
      if (cont != null) {
        toBeParsed.remove(cont);
        checkNullFields(cont);
      }
    });
  }

  void readFields(Object object, JsonValue jsonMap, boolean stripType) {
    if (stripType)
      jsonMap.remove("type");
    readFields(object, jsonMap);
  }

  void readFields(Object object, JsonValue jsonMap) {
    JsonValue research = jsonMap.remove("research");

    toBeParsed.remove(object);
    var type = object.getClass();
    var fields = parser.getFields(type);
    for (JsonValue child = jsonMap.child; child != null; child = child.next) {
      FieldMetadata metadata = fields.get(child.name().replace(" ", "_"));
      if (metadata == null) {
        if (ignoreUnknownFields) {
          Log.warn("[@]: Ignoring unknown field: @ (@)", currentContent.minfo.sourceFile.name(),
              child.name, type.getSimpleName());
          continue;
        } else {
          SerializationException ex = new SerializationException(
              "Field not found: " + child.name + " (" + type.getName() + ")");
          ex.addTrace(child.trace());
          throw ex;
        }
      }
      Field field = metadata.field;
      try {
        if (child.isObject() && child.has("add") && (Seq.class.isAssignableFrom(field.getType())
            || ObjectSet.class.isAssignableFrom(field.getType()))) {
          Object readField = parser.readValue(field.getType(), metadata.elementType,
              child.get("add"), metadata.keyType);
          Object fieldObj = field.get(object);

          if (fieldObj instanceof ObjectSet set) {
            set.addAll((ObjectSet) readField);
          } else if (fieldObj instanceof Seq seq) {
            seq.addAll((Seq) readField);
          } else {
            throw new SerializationException("This should be impossible");
          }
        } else {
          boolean isMap = ObjectMap.class.isAssignableFrom(field.getType())
              || ObjectIntMap.class.isAssignableFrom(field.getType())
              || ObjectFloatMap.class.isAssignableFrom(field.getType());
          boolean mergeMap = isMap && child.has("add") && child.get("add").isBoolean()
              && child.getBoolean("add", false);

          if (mergeMap) {
            child.remove("add");
          }

          Object readField =
              parser.readValue(field.getType(), metadata.elementType, child, metadata.keyType);
          Object fieldObj = field.get(object);

          // if a map has add: true, add its contents to the map instead
          if (mergeMap && (fieldObj instanceof ObjectMap<?, ?>
              || fieldObj instanceof ObjectIntMap<?> || fieldObj instanceof ObjectFloatMap<?>)) {
            if (field.get(object) instanceof ObjectMap<?, ?> baseMap) {
              baseMap.putAll((ObjectMap) readField);
            } else if (field.get(object) instanceof ObjectIntMap<?> baseMap) {
              baseMap.putAll((ObjectIntMap) readField);
            } else if (field.get(object) instanceof ObjectFloatMap<?> baseMap) {
              baseMap.putAll((ObjectFloatMap) readField);
            }
          } else {
            field.set(object, readField);
          }
        }
      } catch (IllegalAccessException ex) {
        throw new SerializationException(
            "Error accessing field: " + field.getName() + " (" + type.getName() + ")", ex);
      } catch (SerializationException ex) {
        ex.addTrace(field.getName() + " (" + type.getName() + ")");
        throw ex;
      } catch (RuntimeException runtimeEx) {
        SerializationException ex = new SerializationException(runtimeEx);
        ex.addTrace(child.trace());
        ex.addTrace(field.getName() + " (" + type.getName() + ")");
        throw ex;
      }
    }

    if (object instanceof UnlockableContent unlock && research != null) {

      // add research tech node
      String researchName;
      ItemStack[] customRequirements;

      // research can be a single string or an object with parent and requirements
      if (research.isString()) {
        researchName = research.asString();
        customRequirements = null;
      } else {
        researchName = research.getString("parent", null);
        customRequirements = research.has("requirements")
            ? parser.readValue(ItemStack[].class, research.get("requirements"))
            : null;
      }

      // remove old node
      TechNode lastNode = TechTree.all.find(t -> t.content == unlock);
      if (lastNode != null) {
        lastNode.remove();
      }

      TechNode node = new TechNode(null, unlock,
          customRequirements == null ? ItemStack.empty : customRequirements);
      LoadedMod cur = currentMod;

      postreads.add(() -> {
        currentContent = unlock;
        currentMod = cur;

        // add custom objectives
        if (research.has("objectives")) {
          node.objectives.addAll(parser.readValue(Objective[].class, research.get("objectives")));
        }

        // all items have a produce requirement unless already specified
        if ((unlock instanceof Item || unlock instanceof Liquid)
            && !node.objectives.contains(o -> o instanceof Produce p && p.content == unlock)) {
          node.objectives.add(new Produce(unlock));
        }

        // remove old node from parent
        if (node.parent != null) {
          node.parent.children.remove(node);
        }

        if (customRequirements == null) {
          node.setupRequirements(unlock.researchRequirements());
        }

        if (research.has("planet")) {
          node.planet = find(ContentType.planet, research.getString("planet"));
        }

        if (research.getBoolean("root", false)) {
          node.name = research.getString("name", unlock.name);
          node.requiresUnlock = research.getBoolean("requiresUnlock", false);
          TechTree.roots.add(node);
        } else {
          if (researchName != null) {
            // find parent node.
            TechNode parent = TechTree.all.find(t -> t.content.name.equals(researchName)
                || t.content.name.equals(currentMod.name + "-" + researchName)
                || t.content.name.equals(SaveVersion.mapFallback(researchName)));

            if (parent == null) {
              Log.warn("Content '" + researchName + "' isn't in the tech tree, but '" + unlock.name
                  + "' requires it to be researched.");
            } else {
              // add this node to the parent
              if (!parent.children.contains(node)) {
                parent.children.add(node);
              }
              // reparent the node
              node.parent = parent;
              node.planet = parent.planet;
            }
          } else {
            Log.warn(unlock.name
                + " is not a root node, and does not have a `parent: ` property. Ignoring.");
          }
        }
      });
    }
  }

  <T> T make(Class<T> type) {
    try {
      Constructor<T> cons = type.getDeclaredConstructor();
      cons.setAccessible(true);
      return cons.newInstance();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  <T> T make(Class<T> type, Class[] classes, Object... objs) {
    try {
      Constructor<T> cons = type.getDeclaredConstructor(classes);
      cons.setAccessible(true);
      return cons.newInstance(objs);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /** Tries to resolve a class from the class type map. */
  <T> Class<T> resolve(ContentType type, String base) {
    return resolve(type, base, null);
  }

  /** Tries to resolve a class from the class type map. */
  <T> Class<T> resolve(ContentType type, String base, Class<T> def) {
    // no base class specified
    if ((base == null || base.isEmpty()) && def != null)
      return def;

    // return mapped class if found in the global map
    var out = TypeClassMaps.get(type,
        !base.isEmpty() && Character.isLowerCase(base.charAt(0)) ? Strings.capitalize(base) : base);
    if (out != null)
      return (Class<T>) out;

    // try to resolve it as a raw class name
    if (base.indexOf('.') != -1) {
      try {
        return (Class<T>) Class.forName(base);
      } catch (Exception ignored) {
        // try to use mod class loader
        try {
          return (Class<T>) Class.forName(base, true, mods.mainLoader());
        } catch (Exception ignore) {
        }
      }
    }

    if (def != null) {
      Log.warn("[@] No type from " + type.toString() + " '" + base + "' found, defaulting to type '"
          + def.getSimpleName() + "'", currentContent == null ? currentMod.name : "");
      return def;
    }
    throw new IllegalArgumentException("Type not found: " + base + "from " + type.toString());
  }

  <T> Class<T> resolve(Class<?> type, String base) {
    return resolve(type, base, null);
  }

  /** Tries to resolve a class from the class type map. */
  <T> Class<T> resolve(Class<?> type, String base, Class<T> def) {
    // no base class specified
    if ((base == null || base.isEmpty()) && def != null)
      return def;

    // return mapped class if found in the global map
    var out = TypeClassMaps.getBase(type,
        !base.isEmpty() && Character.isLowerCase(base.charAt(0)) ? Strings.capitalize(base) : base);
    if (out != null)
      return (Class<T>) out;

    // try to resolve it as a raw class name
    if (base.indexOf('.') != -1) {
      try {
        return (Class<T>) Class.forName(base);
      } catch (Exception ignored) {
        // try to use mod class loader
        try {
          return (Class<T>) Class.forName(base, true, mods.mainLoader());
        } catch (Exception ignore) {
        }
      }
    }

    if (def != null) {
      Log.warn("[@] No type from " + type.toString() + " '" + base + "' found, defaulting to type '"
          + def.getSimpleName() + "'", currentContent == null ? currentMod.name : "");
      return def;
    }
    throw new IllegalArgumentException("Type not found: " + base + "from " + type.toString());
  }

  /** Tries to resolve a class from the class type map. */
  <T> Class<T> resolve(String base) {
    return resolve(base, null);
  }

  /** Tries to resolve a class from the class type map. */
  <T> Class<T> resolve(String base, Class<T> def) {
    // no base class specified
    if ((base == null || base.isEmpty()) && def != null)
      return def;

    // return mapped class if found in the global map
    var out = ClassMap.classes.get(
        !base.isEmpty() && Character.isLowerCase(base.charAt(0)) ? Strings.capitalize(base) : base);
    if (out != null)
      return (Class<T>) out;

    // try to resolve it as a raw class name
    if (base.indexOf('.') != -1) {
      try {
        return (Class<T>) Class.forName(base);
      } catch (Exception ignored) {
        // try to use mod class loader
        try {
          return (Class<T>) Class.forName(base, true, mods.mainLoader());
        } catch (Exception ignore) {
        }
      }
    }

    if (def != null) {
      Log.warn("[@] No type '" + base + "' found, defaulting to type '" + def.getSimpleName() + "'",
          currentContent == null ? currentMod.name : "");
      return def;
    }
    throw new IllegalArgumentException("Type not found: " + base);
  }

  private void readBundle(ContentType type, String name, JsonValue value) {
    UnlockableContent cont =
        locate(type, name) instanceof UnlockableContent ? locate(type, name) : null;

    String entryName = cont == null ? type + "." + currentMod.name + "-" + name + "."
        : type + "." + cont.name + ".";
    I18NBundle bundle = Core.bundle;
    while (bundle.getParent() != null)
      bundle = bundle.getParent();

    if (value.has("name")) {
      if (!Core.bundle.has(entryName + "name")) {
        bundle.getProperties().put(entryName + "name", value.getString("name"));
        if (cont != null)
          cont.localizedName = value.getString("name");
      }
      value.remove("name");
    }

    if (value.has("description")) {
      if (!Core.bundle.has(entryName + "description")) {
        bundle.getProperties().put(entryName + "description", value.getString("description"));
        if (cont != null)
          cont.description = value.getString("description");
      }
      value.remove("description");
    }
  }

  private void readDisplayBundle(ContentType type, String name, JsonValue value) {
    UnlockableContent cont =
        locate(type, name) instanceof UnlockableContent ? locate(type, name) : null;

    String entryName = cont == null ? type + "." + currentMod.name + "-" + name + "."
        : type + "." + cont.name + ".";
    I18NBundle bundle = Core.bundle;
    while (bundle.getParent() != null)
      bundle = bundle.getParent();

    if (value.has("displayName")) {
      if (!Core.bundle.has(entryName + "name")) {
        bundle.getProperties().put(entryName + "name", value.getString("displayName"));
        if (cont != null)
          cont.localizedName = value.getString("displayName");
      }
      value.remove("displayName");
    }

    if (value.has("description")) {
      if (!Core.bundle.has(entryName + "description")) {
        bundle.getProperties().put(entryName + "description", value.getString("description"));
        if (cont != null)
          cont.description = value.getString("description");
      }
      value.remove("description");
    }
  }
}
