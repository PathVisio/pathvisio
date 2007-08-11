<?php

/**
 * AccessList allows to do the following operations:
 *     - create and read a list of user and groups, that are parmitted to access certain section
 *
 * PHP version 5
 *
 * @category   Encryption
 * @package    PageProtectionPlus
 * @author     Fabian Schmitt <fs@u4m.de>, Pawel Wilk <pw@gnu.org>
 * @copyright  2006, 2007 Fabian Schmitt
 * @license    http://www.gnu.org/licenses/gpl.html  General Public License version 2 or higher
 * @version    2.1b
 * @link       http://meta.wikimedia.org/PPP
 */


/**
 * Stores a list of users and groups that are permitted to access a certain 
 * section.
 */
class AccessList
{
    /**
     * Private fields.
     */
    protected $mUsers  = array();
    protected $mGroups = array();
    
    /**
     * Constructor. Creates default user-list and ensures sysop
     * is in allowed groups.
     * @param users Array or comma-separated users-list.
     * @param groups Array or comma-separated groups-list.
     */
    function AccessList($users = null, $groups = null)
    {
        $this->AddUsers($this->getArray($users));
        $this->AddGroups($this->getArray($groups));
        $this->AddGroup("sysop");
    }
    
    /**
     * Checks if parameter is an array of comma-separated list
     * and returns array containing the items.
     * @param $s Array or comma-separated list.
     * @return Array.
     */
    private function getArray($s)
    {
        if (!is_array($s))
        {
            $s = explode(",", $s);
        }
        return $this->removeEmpty($s);
    }
    
    /**
     * Returns parameter-string for protect-tag with permitted
     * users of this object.
     * @return Parameter-string for users.
     */ 
    public function getUsersParam()
    {
        $this->mUsers = $this->removeEmpty($this->mUsers);
        return "users=\"".implode(",", $this->mUsers)."\"";
    }
    
    /**
     * Returns parameter-string for protect-tag with permitted
     * groups of this object.
     * @return Parameter-string for groups.
     */ 
    public function getGroupsParam()
    {
        $this->mGroups = $this->removeEmpty($this->mGroups);
        return "groups=\"".implode(",", $this->mGroups)."\"";
    }
    
    /**
     * Returns a string containing comma-separated list of the
     * permitted users with links to their homepages.
     * @return Comma-separated list of users.
     */
    public function getUserList()
    {
        $usersString = "";
        $users = array();
        // create links to user-pages
        foreach($this->mUsers as $user) {
            $title = Title::makeTitle(NS_USER, $user);
            $userPage = "[[".$title->getNsText().":".$user."|".$user."]]";
            $users[] = $userPage;
        }
        return implode(", ", $users);
    }
    
    /**
    * Returns a string containing comma-separated list of the
    * permitted groups of this object.
    * @return Comma-separated groups-list.
    */
    public function getGroupList() {
        return implode(", ", $this->mGroups);
    }
    
    
    /**
    * Checks if a given user is in this AccessList.
    * @param user User-object to check for.
    * @return true, if user is in list of users or in one of the allowed groups,
    *         false otherwise.
    */
    public function hasAccess(&$user) {
        require_once("includes/User.php");
       
        foreach($this->mGroups as $group) {
            if(in_array($group, $user->mGroups)){
                return true;
            }
        }
        if(in_array("sysop", $user->mGroups)){
            return true;
        }
    
        if (in_array($user->getName(), $this->mUsers)) {
            return true;
        }
    
        return false;
    }
    
    /**
    * Adds a user to the list of permitted users.
    * @param user Username to add
    */
    public function AddUser($user)
    {
        if (!in_array($user, $this->mUsers))
        {
            $this->mUsers[] = $user;
        }
    }
    
    /**
    * Adds a list of users to the list of permitted users.
    * @param user Array of Username to add
    */
    public function AddUsers($users)
    {
        foreach($users as $user)
        {
            $this->AddUser($user);
        }
    }
    
    /**
    * Adds a group to the list of permitted groups.
    * @param group Groupname to add
    */
    public function AddGroup($group)
    {
        if (!in_array($group, $this->mGroups))
        {
            $this->mGroups[] = $group;
        }
    }
    
    /**
    * Adds a list of groups to the list of permitted groups.
    * @param group Array of Groupnames to add
    */
    public function AddGroups($groups)
    {
        foreach ($groups as $group)
        {
            $this->AddGroup($group);
        }
    }
    
    /**
    * Restricts current permissions to the users supplied as
    * parameter and the users that are currently allowed.
    * @param users Users to intersect current users-list with.
    */
    public function RestrictUsers($users)
    {
        $this->mUsers = $this->intersect($this->mUsers,
                                            $this->getArray($users));
    }
    
    /**
    * Restricts current permissions to the groups supplied as
    * parameter and the groups that are currently allowed.
    * @param groups Groups to intersect current groups-list with.
    */
    public function RestrictGroups($groups)
    {
        $this->mGroups = $this->intersect($this->mGroups,
                                            $this->getArray($groups));
    }
    
    /**
    * Intersects two array and returns the result. If one of the given arrays
    * is empty, the other one will be returned.
    * @param a1 First array
    * @param a2 Second array
    * @return Intersection
    */
    private function intersect($a1, $a2) {
        if (count($a1) == 0) {
            return $a2;
        }
        if (count($a2) == 0) {
            return $a1;
        }
        return array_intersect($a1, $a2);
    }
    
    /**
    * Removes empty fields from an array.
    * @param arr Array.
    * @return Array without fields that contain empty strings.
    */
    private function removeEmpty($arr) {
        foreach ($arr as $i => $a) {
            if ($a == "") {
                unset($arr[$i]);
            }
        }
        return $arr;
    }
}

?>
