const fs = require("node:fs");
const path = require("node:path");
const { spawnSync } = require("node:child_process");

const root = process.cwd();
const project = path.join(root, "jpacman");
const generatedDirectories = ["build", ".gradle", "bin", ".idea"];
const generatedFiles = [".classpath", ".project"];

function run(command, args, options = {}) {
  const result = spawnSync(command, args, {
    cwd: root,
    encoding: "utf8",
    stdio: options.inherit ? "inherit" : "pipe",
    shell: options.shell || false,
  });
  if (!options.inherit && result.stdout) process.stdout.write(result.stdout);
  if (!options.inherit && result.stderr) process.stderr.write(result.stderr);
  return result;
}

if (!fs.existsSync(project) || !fs.statSync(project).isDirectory()) {
  throw new Error("Expected project directory jpacman was not found.");
}

console.log("Initial git status:");
const initialStatus = run("git", ["status", "--short"]);
if (initialStatus.status !== 0) throw new Error("Unable to read git status.");

const rootEntries = fs.readdirSync(root);
const allowedRootEntries = new Set([".git", "jpacman", ".vscode", ".migrate-layout.js"]);
const conflicts = rootEntries.filter((entry) => !allowedRootEntries.has(entry));
if (conflicts.length) {
  throw new Error(`Root conflicts detected; no project files moved: ${conflicts.join(", ")}`);
}

const vscode = path.join(root, ".vscode");
if (fs.existsSync(vscode)) {
  const entries = fs.readdirSync(vscode);
  if (entries.length !== 1 || entries[0] !== "tasks.json") {
    throw new Error("Unexpected .vscode contents; refusing to remove them.");
  }
}

const projectEntries = fs.readdirSync(project);
const destinationConflicts = projectEntries.filter((entry) =>
  fs.existsSync(path.join(root, entry))
);
if (destinationConflicts.length) {
  throw new Error(
    `Root conflicts detected; no project files moved: ${destinationConflicts.join(", ")}`
  );
}

for (const entry of generatedDirectories) {
  fs.rmSync(path.join(project, entry), { recursive: true, force: true });
}
for (const entry of generatedFiles) {
  fs.rmSync(path.join(project, entry), { force: true });
}

for (const entry of fs.readdirSync(project)) {
  fs.renameSync(path.join(project, entry), path.join(root, entry));
}
if (fs.readdirSync(project).length !== 0) {
  throw new Error("Project directory was not empty after moving its contents.");
}
fs.rmdirSync(project);

if (fs.existsSync(vscode)) fs.rmSync(vscode, { recursive: true });

console.log("Project contents moved to repository root.");
console.log(`Root layout: ${fs.readdirSync(root).sort().join(", ")}`);

console.log("Running Gradle test suite from repository root:");
const test = run("cmd.exe", ["/c", "gradlew.bat", "test"], { inherit: true });

for (const entry of generatedDirectories) {
  fs.rmSync(path.join(root, entry), { recursive: true, force: true });
}
for (const entry of generatedFiles) {
  fs.rmSync(path.join(root, entry), { force: true });
}

fs.rmSync(path.join(root, ".migrate-layout.js"), { force: true });

console.log("Final git status:");
const finalStatus = run("git", ["status", "--short"]);
if (finalStatus.status !== 0) throw new Error("Unable to verify final git status.");
console.log(`Root layout: ${fs.readdirSync(root).sort().join(", ")}`);
if (test.status !== 0) process.exitCode = test.status || 1;
