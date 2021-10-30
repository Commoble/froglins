package commoble.froglins.data;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import net.minecraft.tags.TagLoader;
import net.minecraft.util.profiling.ProfilerFiller;

/**
 * Manager for tag-like objects.
 * Tags are not synced.
 */
public class FakeTagManager<T> implements PreparableReloadListener
{
	private static final Logger LOGGER = LogManager.getLogger();

	private final TagLoader<T> tagLoader;
	private volatile TagCollection<T> tags = TagCollection.empty();

	/**
	 * @param lookupFunction Function that returns a T given an id. Can return null if no object exists for that ID.
	 * @param folder Data folder to look for tags in, e.g. "tags/mob_effects"
	 */
	public FakeTagManager(Function<ResourceLocation, T> lookupFunction, String folder)
	{
		this.tagLoader = new TagLoader<>(id -> Optional.ofNullable(lookupFunction.apply(id)), folder);
	}

	public TagCollection<T> getTags()
	{
		return this.tags;
	}

	public Tag<T> getTag(ResourceLocation id)
	{
		return this.tags.getTagOrEmpty(id);
	}

	public CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier barrier, ResourceManager manager, ProfilerFiller workerProfiler, ProfilerFiller mainProfiler,
		Executor workerExecutor, Executor mainExecutor)
	{
		return CompletableFuture.supplyAsync(() -> this.tagLoader.load(manager), workerExecutor)
			.thenCompose(barrier::wait)
			.thenAcceptAsync(this::buildTags, mainExecutor);
	}
	
	private void buildTags(Map<ResourceLocation,Tag.Builder> builders)
	{
		this.tags = this.tagLoader.build(builders);
	}
}
