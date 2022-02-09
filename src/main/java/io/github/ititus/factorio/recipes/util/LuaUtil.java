package io.github.ititus.factorio.recipes.util;

import io.github.ititus.factorio.recipes.FactorioRecipes;
import io.github.ititus.commons.function.BiIntObjFunction;
import io.github.ititus.commons.math.number.BigRational;
import org.luaj.vm2.*;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class LuaUtil {

    private LuaUtil() {
    }

    public static LuaTable getTable(LuaTable t, String key) {
        return t.rawget(key).checktable();
    }

    public static LuaTable getTableOrEmpty(LuaTable t, String key) {
        return t.rawget(key).opttable(LuaValue.tableOf());
    }

    public static boolean getBoolean(LuaTable t, String key, boolean default_) {
        return getOptionalBoolean(t, key).orElse(default_);
    }

    public static int getInt(LuaTable t, String key, int default_) {
        return getOptionalInt(t, key).orElse(default_);
    }

    public static double getDouble(LuaTable t, String key, double default_) {
        return getOptionalDouble(t, key).orElse(default_);
    }

    public static String getString(LuaTable t, String key, String default_) {
        return getOptionalString(t, key).orElse(default_);
    }

    public static BigRational getBigRational(LuaTable t, String key, BigRational default_) {
        return getOptionalBigRational(t, key).orElse(default_);
    }

    public static <T> T getObject(LuaTable t, String key, Function<LuaValue, T> fct, T default_) {
        return getOptional(t, key, fct).orElse(default_);
    }

    public static int getInt(LuaTable t, String key) {
        return getOptionalInt(t, key).orElseThrow();
    }

    public static String getString(LuaTable t, String key) {
        return getOptionalString(t, key).orElseThrow();
    }

    public static BigRational getBigRational(LuaTable t, String key) {
        return getOptionalBigRational(t, key).orElseThrow();
    }

    public static Optional<Boolean> getOptionalBoolean(LuaTable t, String key) {
        LuaValue v = t.rawget(key);
        return v.isnil() ? Optional.empty() : Optional.of(v.checkboolean());
    }

    public static OptionalInt getOptionalInt(LuaTable t, String key) {
        LuaValue v = t.rawget(key);
        if (v.isnumber() && !v.isint()) {
            throw new RuntimeException(v + " is not an integer");
        }
        return v.isnil() ? OptionalInt.empty() : OptionalInt.of(v.checkint());
    }

    public static OptionalDouble getOptionalDouble(LuaTable t, String key) {
        LuaValue v = t.rawget(key);
        return v.isnil() ? OptionalDouble.empty() : OptionalDouble.of(v.checkdouble());
    }

    public static Optional<String> getOptionalString(LuaTable t, String key) {
        LuaValue v = t.rawget(key);
        return v.isnil() ? Optional.empty() : Optional.of(v.checkjstring());
    }

    public static Optional<BigRational> getOptionalBigRational(LuaTable t, String key) {
        LuaValue v = t.rawget(key);

        if (v.isnil()) {
            return Optional.empty();
        } else if (v.type() == LuaValue.TNUMBER) {
            if (v.isint()) {
                return Optional.of(BigRational.of(v.checkint()));
            }

            return Optional.of(BigRational.of(v.checkdouble()));
        } else if (v.type() == LuaValue.TSTRING) {
            return Optional.of(BigRational.of(v.checkjstring()));
        }

        throw new IllegalArgumentException(v + " is not a number or string");
    }

    public static <T> Optional<T> getOptional(LuaTable t, String key, Function<LuaValue, T> fct) {
        LuaValue v = t.rawget(key);
        return v.isnil() ? Optional.empty() : Optional.of(fct.apply(v));
    }

    public static <T> List<T> getList(LuaTable t, String key, Function<LuaTable, T> fct) {
        return List.copyOf(luaTableToList(t.rawget(key).opttable(LuaTable.tableOf()),
                (k_, v) -> fct.apply(v.checktable())));
    }

    public static Set<String> getStringSet(LuaTable t, String key) {
        return Set.copyOf(luaTableToSet(t.rawget(key).opttable(LuaTable.tableOf()), (k_, v) -> v.checkjstring()));
    }

    public static <T> List<T> luaArrayToList(LuaTable t, BiIntObjFunction<LuaValue, T> fct) {
        List<T> list = new ArrayList<>();

        LuaValue k = LuaInteger.ZERO;
        while (true) {
            Varargs n = t.inext(k);
            if ((k = n.arg1()).isnil()) {
                break;
            }
            if (n.narg() != 2) {
                throw new RuntimeException();
            }
            list.add(fct.apply(n.arg(1).checkint(), n.arg(2)));
        }

        return list;
    }

    public static <T> Set<T> luaArrayToSet(LuaTable t, BiIntObjFunction<LuaValue, T> fct) {
        Set<T> set = new HashSet<>();

        LuaValue k = LuaInteger.ZERO;
        while (true) {
            Varargs n = t.inext(k);
            if ((k = n.arg1()).isnil()) {
                break;
            }
            if (n.narg() != 2) {
                throw new RuntimeException();
            }
            set.add(fct.apply(n.arg(1).checkint(), n.arg(2)));
        }

        return set;
    }

    public static <T> List<T> luaTableToList(LuaTable t, BiFunction<LuaValue, LuaValue, T> fct) {
        List<T> list = new ArrayList<>();

        LuaValue key = LuaValue.NIL;
        while (true) {
            Varargs next = t.next(key);
            if ((key = next.arg1()).isnil()) {
                break;
            }
            if (next.narg() != 2) {
                throw new RuntimeException();
            }
            list.add(fct.apply(next.arg(1), next.arg(2)));
        }

        return list;
    }

    public static <T> Set<T> luaTableToSet(LuaTable t, BiFunction<LuaValue, LuaValue, T> fct) {
        Set<T> set = new HashSet<>();

        LuaValue key = LuaValue.NIL;
        while (true) {
            Varargs next = t.next(key);
            if ((key = next.arg1()).isnil()) {
                break;
            }
            if (next.narg() != 2) {
                throw new RuntimeException();
            }
            set.add(fct.apply(next.arg(1), next.arg(2)));
        }

        return set;
    }

    public static <K, V> Map<K, V> luaTableToMap(LuaTable t, BiFunction<LuaValue, LuaValue, K> keyFct,
                                                 BiFunction<LuaValue, LuaValue, V> valueFct) {
        Map<K, V> map = new HashMap<>();

        LuaValue key = LuaValue.NIL;
        while (true) {
            Varargs next = t.next(key);
            if ((key = next.arg1()).isnil()) {
                break;
            }
            if (next.narg() != 2) {
                throw new RuntimeException();
            }

            LuaValue k = next.arg(1);
            LuaValue v = next.arg(2);
            map.put(keyFct.apply(k, v), valueFct.apply(k, v));
        }

        return map;
    }

    public static LuaValue loadLuaFile(Globals globals, Path parent, String path) {
        LuaValue v;

        Path file = parent != null ? parent.resolve(path) : Path.of(path);
        try (Reader r = Files.newBufferedReader(file)) {
            v = globals.load(r, file.getFileName().toString());
        } catch (IOException e) {
            try {
                file = Path.of(FactorioRecipes.class.getResource('/' + file.toString()).toURI());
                try (Reader r = Files.newBufferedReader(file)) {
                    v = globals.load(r, file.getFileName().toString());
                } catch (IOException e2) {
                    e2.addSuppressed(e);
                    throw new UncheckedIOException(e2);
                }
            } catch (URISyntaxException e1) {
                e1.addSuppressed(e);
                throw new RuntimeException(e1);
            }
        }

        return v.call();
    }
}
