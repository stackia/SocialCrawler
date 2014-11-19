package core.storage;

/**
 * Project: SocialCrawler
 * Package: core.storage
 * Created by Stackia <jsq2627@gmail.com> on 11/19/14.
 */
public interface UserStorage<T> {

    /**
     * Insert a new user into the storage.
     *
     * @param newUser The new user.
     * @return true if successful, otherwise false.
     */
    public boolean insert(T newUser);

    /**
     * Delete a user from the storage.
     * <p/>
     * A record whose primary key matches the user will be deleted from database.
     *
     * @param userToDelete The user to delete.
     * @return true if successful, otherwise false.
     */
    public boolean delete(T userToDelete);

    /**
     * Find a user at a specific location in database.
     *
     * @param offset Location offset.
     * @return The user if successful, or null if there doesn't exist a user at that location.
     */
    public T find(long offset);

    /**
     * Check if a user exists in the storage.
     *
     * Match behaves according to the user's primary key.
     *
     * @param user The user to check.
     * @return true if exists, otherwise false.
     */
    public boolean exists(T user);

    /**
     * Update an user in the storage.
     *
     * Match behaves according to the user's primary key.
     *
     * @param user The user with updated information.
     * @return true if successful, otherwise false.
     */
    public boolean update(T user);
}
