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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/10.0.1/STCore.xcframework.zip",
            checksum: "289679feff6cbfb0b8d9ba9119182581e8c673e6900953bdaf68c03fbd4ec143"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/10.0.1/STDatabase.xcframework.zip",
            checksum: "86d3680b23979bf7a083d487514e72098911314161f1e0ac4c227e3180817233"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/10.0.1/STNetwork.xcframework.zip",
            checksum: "6b322a7cd3be9b3dae96141c83affec8b9974c89a027ecdffedfcc84c0d06065"
        ),
    ]
)
