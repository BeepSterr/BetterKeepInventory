package com.beepsterr.betterkeepinventory.Library.Versions;

import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

public class Version implements Comparable<Version> {

    public final int major;
    public final int minor;
    public final int patch;
    public final String flavor; // informational only
    public final int build;     // used in comparison if present

    public Version(String versionStr) {
        String[] mainParts = versionStr.split("-");

        // Parse core version: major.minor.patch
        String[] versionNumbers = mainParts[0].split("\\.");
        if (versionNumbers.length != 3) {
            throw new IllegalArgumentException("Invalid version format: " + versionStr);
        }

        this.major = Integer.parseInt(versionNumbers[0]);
        this.minor = Integer.parseInt(versionNumbers[1]);
        this.patch = Integer.parseInt(versionNumbers[2]);

        // Handle flavor/build info
        if (mainParts.length >= 2) {
            this.flavor = mainParts[1].toUpperCase();
        } else {
            this.flavor = "STABLE";
        }

        if (mainParts.length >= 3) {
            this.build = Integer.parseInt(mainParts[2]);
        } else {
            this.build = 0;
        }
    }

    public static String getCommitHash() {
        Properties properties = new Properties();
        try {
            properties.load(Version.class.getClassLoader().getResourceAsStream("application.properties"));
            return properties.getProperty("git.commit.id.abbrev");
        } catch (IOException e) {
            e.printStackTrace();
            return "unknown";
        }
    }

    @Override
    public int compareTo(Version other) {
        if (this.major != other.major) return Integer.compare(this.major, other.major);
        if (this.minor != other.minor) return Integer.compare(this.minor, other.minor);
        if (this.patch != other.patch) return Integer.compare(this.patch, other.patch);
        // Compare flavor: STABLE is considered newer than any other flavor
        if (!this.flavor.equals(other.flavor)) {
            if (this.flavor.equals("STABLE")) return 1;
            if (other.flavor.equals("STABLE")) return -1;
            return this.flavor.compareTo(other.flavor);
        }
        return Integer.compare(this.build, other.build);
    }

    @Override
    public String toString() {
        String base = major + "." + minor + "." + patch;
        if (!flavor.equals("STABLE") || build > 0) {
            base += "-" + flavor;
        }
        if (build > 0) {
            base += "-" + build;
        }
        return base;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Version v)) return false;
        return this.compareTo(v) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(major, minor, patch, build);
    }
}
