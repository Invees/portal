import com.google.gson.annotations.SerializedName;
import com.itextpdf.io.util.FileUtil;
import de.invees.portal.common.model.ApiInterfaceIgnore;
import de.invees.portal.common.model.Model;
import org.reflections.Reflections;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.*;

public class Main {

  public static void main(String[] args) throws IOException {
    Main.generate();
  }

  public static void generate() throws IOException {
    Reflections reflections = new Reflections("de.invees");
    File file = new File("model");
    FileUtil.deleteFile(file);
    Set<Class<? extends Model>> classes = reflections.getSubTypesOf(Model.class);

    for (Class clazz : classes) {
      generateClass(clazz);
    }
  }

  public static void generateClass(Class clazz) throws IOException {
    if (clazz.getAnnotation(ApiInterfaceIgnore.class) != null) {
      return;
    }
    if (clazz.isEnum()) {
      return;
    }
    String path = clazz.getName()
        .replace("de.invees.portal.common.model.", "")
        .replace(".", "/") + ".ts";
    String name = clazz.getSimpleName();

    List<String> imports = new ArrayList<>();
    StringBuilder builder = new StringBuilder();
    builder.append("export default interface " + name + " {\n");
    for (Field field : clazz.getDeclaredFields()) {
      if (Modifier.isStatic(field.getModifiers())) {
        continue;
      }
      imports.add(mappedImport(clazz, field.getType(), field));
      String fieldName = field.getName();
      SerializedName serializedName = field.getAnnotation(SerializedName.class);
      if (serializedName != null) {
        fieldName = serializedName.value();
      }
      builder.append("  " + fieldName + ": " + mappedField(field.getType(), field) + ";\n");
    }
    builder.append("}");
    StringBuilder importBuilder = new StringBuilder();
    boolean hasImport = false;
    List<String> doneImports = new ArrayList<>();
    for (String impor : imports) {
      if (impor == null) {
        continue;
      }
      if (doneImports.contains(impor)) {
        continue;
      }
      importBuilder.append(impor);
      doneImports.add(impor);
      hasImport = true;
    }
    if (hasImport) {
      importBuilder.append("\n");
    }
    importBuilder.append(builder);
    File file = new File("model/" + path);
    if (!file.getParentFile().exists()) {
      file.getParentFile().mkdirs();
    }
    BufferedWriter out = new BufferedWriter(new FileWriter(file), 32768);
    out.write(importBuilder + "\n");
    out.flush();
    out.close();
  }

  private static String mappedImport(Class<?> src, Class<?> clazz, Field field) {
    if (!clazz.getName().startsWith("de.invees.portal.common.model.") && clazz != List.class && clazz != Map.class) {
      return null;
    }
    String simpleName = clazz.getSimpleName();
    String name = clazz.getName();

    if (clazz == List.class) {
      ParameterizedType type = (ParameterizedType) field.getGenericType();
      simpleName = ((Class<?>) type.getActualTypeArguments()[0]).getSimpleName();
      name = ((Class<?>) type.getActualTypeArguments()[0]).getName();
    }
    if (clazz == Map.class) {
      return null;
    }
    if (clazz.isEnum()) {
      return null;
    }
    if (!name.startsWith("de.invees.portal.common.model.")) {
      return null;
    }
    if (name.equals(src.getName())) {
      return null;
    }
    return "import " + simpleName + " from \"@/model/" + name
        .replace("de.invees.portal.common.model.", "")
        .replace(".", "/") + ".ts\";\n";
  }

  private static String mappedField(Class<?> clazz, Field field) {
    if (clazz == UUID.class) {
      return "string";
    }
    if (clazz == long.class || clazz == Long.class || clazz == int.class
        || clazz == Integer.class || clazz == double.class || clazz == Double.class) {
      return "number";
    }
    if (clazz == boolean.class || clazz == Boolean.class) {
      return "boolean";
    }
    if (clazz == List.class) {
      ParameterizedType type = (ParameterizedType) field.getGenericType();
      return "Array<" + mappedField((Class<?>) type.getActualTypeArguments()[0], null) + ">";
    }
    if (clazz == Map.class) {
      ParameterizedType type = (ParameterizedType) field.getGenericType();
      return "any";
    }
    if (clazz == String.class) {
      return "string";
    }
    if (clazz.getSimpleName().equalsIgnoreCase("Object")) {
      return "any";
    }
    if (clazz.isEnum()) {
      return "string";
    }
    if (clazz == byte[].class) {
      return "Blob";
    }
    return clazz.getSimpleName();
  }

}
