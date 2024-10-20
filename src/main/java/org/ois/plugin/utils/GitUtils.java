package org.ois.plugin.utils;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.nio.file.Path;

/**
 * Git Utilities
 */
public class GitUtils {

    /**
     * Clone a public GIT repository to the file system
     * @param repositoryURL - the public repository to clone
     * @param branch - the repository branch to clone
     * @param destinationFolder - the destination directory of the cloned repository
     * @return the repository object that was cloned.
     * @throws GitAPIException - on git API call issue
     */
    public static Git cloneRepoByTag(String repositoryURL, String branch, Path destinationFolder) throws GitAPIException {
        return Git.cloneRepository()
                .setURI(repositoryURL)
                .setDirectory(destinationFolder.toFile())
                .setBranch(branch)
                .call();
    }
}
