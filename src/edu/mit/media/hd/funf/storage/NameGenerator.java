package edu.mit.media.hd.funf.storage;

import edu.mit.media.hd.funf.HashUtil;
import android.content.Context;
import android.telephony.TelephonyManager;

public interface NameGenerator {
	
	/**
	 * Generate a name using the name that was passed in.
	 * @param name
	 * @return
	 */
	public String generateName(final String name);
	
	public static class IdentityNameGenerator implements NameGenerator {
		@Override
		public String generateName(final String name) {
			return name;
		}
	}

	public static class TimestampNameGenerator implements NameGenerator {
		@Override
		public String generateName(final String name) {
			return name == null ? null : System.currentTimeMillis() + "_" + name;
		}
	}
	
	public static class ConstantNameGenerator implements NameGenerator {
		
		private final String prefix, suffix;
		
		public ConstantNameGenerator(String prefix, String suffix) {
			this.prefix = prefix;
			this.suffix = suffix;
		}
		
		@Override
		public String generateName(final String name) {
			return name == null ? null : prefix + name + suffix;
		}
	}
	
	/**
	 * Applies the name generators in the order they were passed in.
	 *
	 */
	public static class CompositeNameGenerator implements NameGenerator {
		
		private final NameGenerator[] nameGenerators;
		
		public CompositeNameGenerator(NameGenerator... nameGenerators) {
			assert nameGenerators != null;
			this.nameGenerators = new NameGenerator[nameGenerators.length];
			System.arraycopy(nameGenerators, 0, this.nameGenerators, 0, nameGenerators.length);
		}
		
		@Override
		public String generateName(final String name) {
			if (name == null) {
				return null;
			}
			String transformedName = name;
			for (NameGenerator nameGenerator : nameGenerators) {
				transformedName = nameGenerator.generateName(transformedName);
			}
			return transformedName;
		}
	}
	
	public static class SystemUniqueTimestampNameGenerator implements NameGenerator {

		private final NameGenerator delegate;
		
		public SystemUniqueTimestampNameGenerator(Context context) {
			String deviceId = ((TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
			String hashedDeviceId = HashUtil.oneWayHashString(deviceId);
			delegate = new CompositeNameGenerator(new TimestampNameGenerator(), new ConstantNameGenerator(hashedDeviceId + "_", ""));		
		}
		
		
		@Override
		public String generateName(String name) {
			return delegate.generateName(name);
		}		
	}
	
	public static class RequiredSuffixNameGenerator implements NameGenerator {

		private final String requiredSuffix;
		
		public RequiredSuffixNameGenerator(String requiredSuffix) {
			this.requiredSuffix = requiredSuffix;
		}
		
		@Override
		public String generateName(String name) {
			if (name != null && !name.toLowerCase().endsWith(requiredSuffix)) {
				name = name + requiredSuffix;
			}
			return name;
		}		
	}
}
