# PAM User on Remote PAM

Consider two isolated PAM environments. Each of these can be clustered, but the two environments are not part of the same PAM cluster. The aim here is to use the `PAM User` connector for managing PAM login users in one PAM environment from a different PAM environment. Ideally you would use automated login to the PAM GUI using a password not visible to a user. This is described elsewhere.

![PAM User for Breakglass](/docs/images/PAMUser-RemotePAM.png)

In the example here there are many moving parts with two different PAM environments and users, and target accounts created in one environment being linked to target accounts in the other. In the description below, the two environments are named `PAM-01` and `PAM-02`, and the configuration in PAM is clearly marked with the environment it belongs to.

- **PAM-01** is the environment where accounts are managed from.
- **PAM-02** is the environment where the local PAM user exists.

In the example, PAM-01 treats PAM-02 as an end-point through the `PAM User` connector to manage its local PAM users. The same approach can be reversed (PAM-02 managing PAM-01), but that is not covered here.

---

# PAM-02: PAM setup

PAM-02 is where the local PAM user exists. From PAM-01’s perspective, PAM-02 is simply another target environment where local accounts are defined and managed.

## PAM-02: Global Settings

An important update on PAM-02 is the password length for local PAM users. The default length is 16 characters, which is likely insufficient. Edit the PAM System **Global Settings** and change the password length to **72 characters** (maximum length).

![Password Length for PAM Users](/docs/images/GlobalSettings-User-Password-Length.png)

## PAM-02: PAM User

In this example, the local PAM user in PAM-02 has the role **Global Administrator**. You may assign other roles if needed. If so, the ApiKey associated with that user must have permission to list and search target accounts and target servers. It is also possible to limit the scope of accounts accessible to the ApiKey via a target group, though this is not used in this example.

![PAM User - Basic Info](/docs/images/PAM-02-User-1.png)

The role for the PAM user in this example is **Global Administrator**.

![PAM User - Roles](/docs/images/PAM-02-User-2.png)

A Global Administrator must also have at least one **Credential Manager Group** assigned.

![PAM User - Credential Manager Groups](/docs/images/PAM-02-User-3.png)

Finally, define an ApiKey for the local PAM user. Initially, when the ApiKey is created the ID is `0`, and will be updated once the user is saved.

![PAM User - ApiKey](/docs/images/PAM-02-User-4.png)

## PAM-02: ApiKey

The ApiKey is used when the `PAM User` connector requests a password update for a PAM user.

The ApiKey is created as a standard ApiKey when the local PAM user is created. In this example, the password length is increased and password age enforcement is disabled.

### PAM-02 – Password Composition Policy

The PCP used for the ApiKey in PAM-02 will also be referenced from PAM-01. In PAM-02 this ApiKey must be **static**—that is, PAM-02 must *not* automatically rotate the password. Password updates will be triggered from PAM-01.

![PCP - ApiKey Static](/docs/images/PCP-ApiKey-static.png)

### PAM-02: ApiKey – TargetApplication

To keep the static ApiKey separate from standard ApiKeys, create a new target application `ApiKey-static` using a PCP without password age enforcement. Password length must be sufficiently high.

![TargetApplication - ApiKey Static](/docs/images/PAM-02-TargetApplication-ApiKey.png)

### PAM-02: ApiKey – TargetAccount

Locate the ApiKey created earlier when the local PAM user was saved.  
Change its password to a new random value based on the PCP configuration for `ApiKey-static`.  
Record both **username** and **password**—they will be used in PAM-01.

![TargetAccount - ApiKey](/docs/images/PAM-02-TargetAccount-ApiKey.png)

![TargetAccount - ApiKey Password](/docs/images/PAM-02-TargetAccount-ApiKey-Password.png)

---

# PAM-01: PAM setup

PAM-01 is where the local PAM user and ApiKey on PAM-02 will be managed. From PAM-01’s perspective, PAM-02 is an end-point using the `PAM User` connector.

## PAM-01: Password Composition Policy

Passwords for the target accounts created in PAM-01 (representing the local PAM user and ApiKey in PAM-02) must be automatically rotated by PAM-01. Create a new PCP using **password age enforcement**.  
The password length for this PCP must not exceed the maximum password length for PAM user accounts in PAM-02 (configured under Global Settings).

![PCP - PAM User Dynamic](/docs/images/PCP-PAMUser-Passwords-dynamic.png)

> [!NOTE]
> The password composition in this PCP must align with the PCP used on PAM-02 for ApiKeys and with the local PAM user password settings.  
> The PCP on PAM-01 is **dynamic**; the PCP on PAM-02 is **static**.

### PAM-01: TargetApplication

The same target application is used for both the ApiKey and the local PAM user existing on PAM-02.

![TargetApplication - PAM User](/docs/images/PAM-01-TargetApplication-1.png)

Use defaults on the **PAM User** tab, but ensure communication from the TCF server in PAM-01 to a PAM server in PAM-02 is possible.

![TargetApplication - PAM User](/docs/images/PAM-01-TargetApplication-2.png)

In the **PAM User (remote)** tab confirm that **PAM is remote** is selected.

![TargetApplication - PAM User](/docs/images/PAM-01-TargetApplication-3.png)

### PAM-01: ApiKey – TargetAccount

Create a target account for the ApiKey that PAM-01 will use when sending API commands to PAM-02.  
Use the account name of the real ApiKey from PAM-02.  
Enter the **initial password** exactly as generated in PAM-02.

![TargetAccount - ApiKey](/docs/images/PAM-01-TargetAccount-ApiKey-1.png)

In the **Password** tab, configure synchronization to update both PAM and the end-point.

In the **PAM User** tab, set the account type to **ApiKey**.  
In this example there is no master account; leave it empty.

![TargetAccount - ApiKey](/docs/images/PAM-01-TargetAccount-ApiKey-2.png)

### PAM-01: PAM User – TargetAccount

Create a target account named exactly like the local PAM user on PAM-02.  
Generate a new random password.

![TargetAccount - PAM User](/docs/images/PAM-01-TargetAccount-User-1.png)

In the **Password** tab, configure synchronization to update both PAM and the end-point.

In the **PAM User** tab, set account type to **PAM User** and specify the ApiKey target account (created earlier) as the **master account**.  
This master account is not a real ApiKey in PAM-01, but a target account using the `PAM User` connector.

![TargetAccount - PAM User](/docs/images/PAM-01-TargetAccount-User-2.png)

When saved, PAM-01 will update the local PAM user password in PAM-02 with a new random value.

---

# Improvements and things to consider

- You can configure two ApiKey accounts in PAM-02 for the same PAM user and let PAM-01 manage both. Each can be configured as master for the other.
- Use target groups to restrict what an ApiKey can access.
- Limit ApiKey permissions to the minimum required for updating passwords on local PAM users.

---

# Error handling

If the API call to update the local PAM user on PAM-02 fails, the API return code can be found in  
`$CATALINA_HOME/logs/catalina.log` on the TCF server associated with PAM-01.
