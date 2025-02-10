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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/1.0.5/STCore.xcframework.zip",
            checksum: "d16fbc7f0a8735dc1297bc3107e6753e5017871512654b5f462c3143014b4d6f"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/1.0.5/STDatabase.xcframework.zip",
            checksum: "d61ecd4decff07a4d34ef9d643540e8d4933085a10e22410b6b631f12d147410"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/1.0.5/STNetwork.xcframework.zip",
            checksum: "889471505fd1c3af7d7f8975284e37da317b817f167ce686651081271fbce0a6"
        ),
    ]
)
