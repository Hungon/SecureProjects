<h1>SecureProjects</h1>
<p>
I'm going to implement functions and services to concern about security based on Android Standard Security Policy.
</p>

<h3>SharedMemory</h3>
<p>Demonstorate protection control over processes.</p>
<p>
First of all, you may need to create key store file if you want to try this with your own key store file.
</p>
<h4>Command to check hash of keystore file</h4>
keytool -exportcert -alias your-alias -keystore ~/your-keystore-path | openssl sha1 -binary | openssl sha256
<li><a href="https://developer.android.com/reference/android/os/SharedMemory">SharedMemory</a></li>
