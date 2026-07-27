// swift-tools-version:5.10
import PackageDescription

let package = Package(
    name: "SwissTransfer-Multiplatform",
    platforms: [
        .iOS(.v14),
        .macOS(.v11)
    ],
    products: [
        .library(name: "STCore", targets: ["Core"]),
        .library(name: "STDatabase", targets: ["Database"]),
        .library(name: "STNetwork", targets: ["Network"])
    ],
    targets: [
        .binaryTarget(
            name: "Core",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/10.1.2-snapshot/STCore.xcframework.zip",
            checksum: "db84bc16d32cec8d7e3def939002e0b795d3e28d3aa2c9e7abcc43a68019eaf9"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/10.1.2-snapshot/STDatabase.xcframework.zip",
            checksum: "bde695788a864045e2a4609e0c1e8ae4b579d746d749ec361a158e3115720c46"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/10.1.2-snapshot/STNetwork.xcframework.zip",
            checksum: "96558840d2f7106d9f754bc85cd6eac13da9c405ddc53d983f9614edd4dd97a5"
        ),
    ]
)
