/*
 * Copyright (c) 2019-2025 Team Galacticraft
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package dev.galacticraft.mod.content.block.special.slimeling_egg;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.Util;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import org.joml.Vector3f;

import java.util.Map;
import java.util.stream.Stream;

public record SlimelingEggColor(String name, Vector3f color) {
    private static final Map<String, SlimelingEggColor> COLORS = new Object2ObjectArrayMap<>();
    public static final Codec<SlimelingEggColor> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("name").forGetter(SlimelingEggColor::name),
                    ExtraCodecs.VECTOR3F.fieldOf("color").forGetter(SlimelingEggColor::color)
            ).apply(instance, SlimelingEggColor::new));

    public static final SlimelingEggColor RED = register(new SlimelingEggColor("red", new Vector3f(1.0f, 0.0f, 0.0f)));
    public static final SlimelingEggColor BLUE = register(new SlimelingEggColor("green", new Vector3f(0.0f, 0.0f, 1.0f)));
    public static final SlimelingEggColor YELLOW = register(new SlimelingEggColor("yellow", new Vector3f(1.0f, 1.0f, 0.0f)));

    private static SlimelingEggColor register(SlimelingEggColor color) {
        COLORS.put(color.name(), color);
        return color;
    }

    public static Stream<SlimelingEggColor> values() {
        return COLORS.values().stream();
    }

    public static Vector3f getRandomColor(RandomSource randomSource) {
        return Util.getRandom(values().map(SlimelingEggColor::color).toList(), randomSource);
    }
}