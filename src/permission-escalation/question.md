# Question 8: Permission Escalation — Can User X Reach the Admin Group?

Groups can be members of other groups (nesting), and a user can be a direct
member of multiple groups. Given a group-membership graph and a starting
user, determine whether that user can transitively reach a target group
(e.g. `"admins"`) through any chain of group memberships.

## Requirements
- Input: `Map<String, List<String>> memberships` — key is a user or group
  name, value is the list of groups it directly belongs to.
- Input: starting user/group and a target group name.
- Output: `true`/`false`, and ideally the path found (useful for an actual
  security report — "here's how they got there").
- The graph can contain cycles (group A is (mistakenly) a member of group B
  which is a member of group A) — must not infinite-loop.

## Interview discussion points
1. **BFS vs DFS.** Either works for plain reachability; BFS naturally gives
   the *shortest* escalation path, which is more useful in a real report
   ("shortest chain of group memberships that grants admin") — mention
   that as the reason to prefer BFS here over DFS.
2. **Cycle handling.** A `visited` set is non-negotiable — group graphs in
   the wild absolutely do contain accidental cycles, and this is exactly
   the kind of edge case an interviewer will probe for after the happy path
   works.
3. **Directed graph, not undirected.** Membership is one-directional
   (member -> group), so don't add reverse edges; reachability must follow
   membership direction only.
4. **Framing:** this is a real Varonis/identity-security product feature —
   "effective permissions" / "who can reach this privileged group" analysis
   is exactly this reachability problem run at scale.

## Complexity
- O(V + E) where V = number of users/groups, E = number of membership edges.
- Space: O(V) for the visited set and BFS queue.
