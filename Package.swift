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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.7.0/STCore.xcframework.zip",
            checksum: "992f795801690c3ec75ba510ad6649090b952fe3d02d2ea4da43549a7cf75a2d"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.7.0/STDatabase.xcframework.zip",
            checksum: "eacb7c4123c0ebbb2d53934da12b7e4c5808fec3f470bb22f31f64fb19b65eb9"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.7.0/STNetwork.xcframework.zip",
            checksum: "3a5185b5e485a6aabcd949c6c46f3e90e5bfae0da0e2216ffa2dc046766c2b8a"
        ),
    ]
)
