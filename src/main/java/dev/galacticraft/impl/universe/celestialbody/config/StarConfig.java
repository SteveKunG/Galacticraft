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

package dev.galacticraft.impl.universe.celestialbody.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.galacticraft.api.gas.GasComposition;
import dev.galacticraft.api.universe.celestialbody.CelestialBodyConfig;
import dev.galacticraft.api.universe.display.CelestialDisplay;
import dev.galacticraft.api.universe.display.ring.CelestialRingDisplay;
import dev.galacticraft.api.universe.galaxy.Galaxy;
import dev.galacticraft.api.universe.position.CelestialPosition;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record StarConfig(
        @NotNull Component name,
        @NotNull Component description,
        @NotNull Optional<ResourceKey<Galaxy>> galaxy,
        @NotNull CelestialPosition<?, ?> position,
        @NotNull CelestialDisplay<?, ?> display,
        @NotNull CelestialRingDisplay<?, ?> ring,
        GasComposition photosphericComposition,
        float gravity,
        double luminance,
        int surfaceTemperature
) implements CelestialBodyConfig {
    public static final Codec<StarConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ComponentSerialization.CODEC.fieldOf("name").forGetter(StarConfig::name),
            ComponentSerialization.CODEC.fieldOf("description").forGetter(StarConfig::description),
            Galaxy.CODEC.optionalFieldOf("galaxy").forGetter(StarConfig::galaxy),
            CelestialPosition.CODEC.fieldOf("position").forGetter(StarConfig::position),
            CelestialDisplay.CODEC.fieldOf("display").forGetter(StarConfig::display),
            CelestialRingDisplay.CODEC.fieldOf("ring").forGetter(StarConfig::ring),
            GasComposition.CODEC.fieldOf("photospheric_composition").forGetter(StarConfig::photosphericComposition),
            Codec.FLOAT.fieldOf("gravity").forGetter(StarConfig::gravity),
            Codec.DOUBLE.fieldOf("luminance").forGetter(StarConfig::luminance),
            Codec.INT.fieldOf("surface_temperature").forGetter(StarConfig::surfaceTemperature)
    ).apply(instance, StarConfig::new));
}
